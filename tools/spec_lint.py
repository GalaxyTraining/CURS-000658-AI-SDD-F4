#!/usr/bin/env python3
"""
spec_lint.py - Validación Spec-First del Catálogo de Productos.

Verifica, ANTES de implementar, que el contrato OpenAPI sea consistente consigo mismo
y con las specs de negocio (specs/domain, specs/adr, specs/requirements, specs/user-stories).
Cuando exista código, también compara los enums Java con el contrato.

Uso:
    python tools/spec_lint.py
Salida: código 0 si no hay errores, 1 si hay errores.
Requiere: pip install pyyaml
"""
import re
import sys
from decimal import Decimal
from pathlib import Path

import yaml

try:
    sys.stdout.reconfigure(encoding="utf-8")  # consola de Windows
except AttributeError:
    pass

ROOT = Path(__file__).resolve().parent.parent
SPECS = ROOT / "specs"
CONTRACT = SPECS / "api" / "catalog-openapi.yaml"
METHODS = ("get", "post", "put", "patch", "delete")

errors, warnings = [], []


def error(rule, msg):
    errors.append(f"[{rule}] {msg}")


def warn(rule, msg):
    warnings.append(f"[{rule}] {msg}")


# ------------------------------------------------------------ lectura de markdown
def md_tables(path):
    """Devuelve las tablas del markdown como lista de (encabezados, filas)."""
    tables, current = [], None
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line.startswith("|"):
            current = None
            continue
        cells = [c.strip() for c in line.strip("|").split("|")]
        if current is None:
            current = (cells, [])
            tables.append(current)
        elif not all(set(c) <= set(":- ") for c in cells):
            current[1].append(cells)
    return tables


def code_of(cell):
    """`DRAFT` -> 'DRAFT'; '—' -> None."""
    m = re.search(r"`([A-Z0-9_]+)`", cell)
    return m.group(1) if m else None


def column(path, header):
    """Valores (códigos) de la primera tabla que tenga la columna `header`."""
    for headers, rows in md_tables(path):
        if header in headers:
            i = headers.index(header)
            return [code_of(r[i]) for r in rows if i < len(r) and code_of(r[i])]
    return None


def lifecycle_transitions():
    for headers, rows in md_tables(SPECS / "domain" / "product-lifecycle.md"):
        if "Desde" in headers and "Hacia" in headers:
            d, h = headers.index("Desde"), headers.index("Hacia")
            return {(code_of(r[d]), code_of(r[h])) for r in rows}
    return set()


# ------------------------------------------------------------ utilidades OpenAPI
def resolve(doc, ref):
    node = doc
    for part in ref[2:].split("/"):
        node = node[part]
    return node


def walk(node, path="#"):
    if isinstance(node, dict):
        yield path, node
        for k, v in node.items():
            yield from walk(v, f"{path}/{k}")
    elif isinstance(node, list):
        for i, v in enumerate(node):
            yield from walk(v, f"{path}/{i}")


def operations(doc):
    for path, item in doc.get("paths", {}).items():
        for method in METHODS:
            if method in item:
                yield path, method, item[method], item


# ------------------------------------------------------------ validador de ejemplos
def validate(doc, schema, value, where, errs, strict=True):
    if "$ref" in schema:
        schema = resolve(doc, schema["$ref"])
    if value is None:
        nullable = schema.get("nullable") or any(
            (resolve(doc, s["$ref"]) if "$ref" in s else s).get("nullable") for s in schema.get("allOf", []))
        if not nullable:
            errs.append(f"{where}: null no permitido")
        return
    for sub in schema.get("allOf", []):
        validate(doc, sub, value, where, errs, strict=False)
    t = schema.get("type")
    if t == "object" or "properties" in schema:
        if not isinstance(value, dict):
            return errs.append(f"{where}: se esperaba un objeto")
        props = schema.get("properties", {})
        for req in schema.get("required", []):
            if req not in value:
                errs.append(f"{where}: falta el campo requerido '{req}'")
        if len(value) < schema.get("minProperties", 0):
            errs.append(f"{where}: debe tener al menos {schema['minProperties']} campo(s)")
        for k, v in value.items():
            if k in props:
                validate(doc, props[k], v, f"{where}.{k}", errs)
            elif strict:
                errs.append(f"{where}: el campo '{k}' no existe en el esquema")
    elif t == "array":
        if not isinstance(value, list):
            return errs.append(f"{where}: se esperaba un array")
        for i, item in enumerate(value):
            validate(doc, schema.get("items", {}), item, f"{where}[{i}]", errs)
    elif t == "string":
        if not isinstance(value, str):
            return errs.append(f"{where}: se esperaba string")
        if "enum" in schema and value not in schema["enum"]:
            errs.append(f"{where}: '{value}' no está en {schema['enum']}")
        if len(value) < schema.get("minLength", 0) or len(value) > schema.get("maxLength", 10**9):
            errs.append(f"{where}: longitud fuera de rango")
        if "pattern" in schema and not re.search(schema["pattern"], value):
            errs.append(f"{where}: '{value}' no cumple el patrón {schema['pattern']}")
    elif t in ("number", "integer"):
        if isinstance(value, bool) or not isinstance(value, (int, float)):
            return errs.append(f"{where}: se esperaba {t}")
        if "minimum" in schema:
            low = schema["minimum"]
            if value < low or (schema.get("exclusiveMinimum") and value == low):
                errs.append(f"{where}: {value} está por debajo del mínimo permitido")
        if "maximum" in schema and value > schema["maximum"]:
            errs.append(f"{where}: {value} supera el máximo {schema['maximum']}")
        if "multipleOf" in schema and Decimal(str(value)) % Decimal(str(schema["multipleOf"])) != 0:
            errs.append(f"{where}: {value} no es múltiplo de {schema['multipleOf']}")


# ------------------------------------------------------------ reglas
def check_refs(doc):
    for where, node in walk(doc):
        ref = node.get("$ref")
        if ref:
            try:
                resolve(doc, ref)
            except (KeyError, TypeError):
                error("REF-01", f"$ref sin resolver: {ref} (en {where})")
            if len(node) > 1:
                error("REF-02", f"{where}: claves junto a $ref se ignoran en OpenAPI 3.0 "
                                f"{sorted(set(node) - {'$ref'})}; usar allOf")


def check_operations(doc):
    tags = {t["name"] for t in doc.get("tags", [])}
    rf_ids = set(re.findall(r"###\s+(RF-\d+)", (SPECS / "requirements" / "functional.md").read_text(encoding="utf-8")))
    us_ids = {f.name.split("-")[0] for f in (SPECS / "user-stories").glob("US*.md")}
    states = set(column(SPECS / "domain" / "product-lifecycle.md", "Código") or [])
    valid_transitions = lifecycle_transitions()
    seen_ids, covered, used_transitions = {}, set(), set()

    for path, method, op, item in operations(doc):
        where = f"{method.upper()} {path}"
        oid = op.get("operationId")
        if not oid:
            error("OP-01", f"{where}: sin operationId")
        elif oid in seen_ids:
            error("OP-01", f"{where}: operationId '{oid}' repetido en {seen_ids[oid]}")
        else:
            seen_ids[oid] = where
        if not op.get("summary"):
            error("OP-02", f"{where}: sin summary")
        if not op.get("tags") or any(t not in tags for t in op["tags"]):
            error("OP-03", f"{where}: tags ausentes o no declarados")

        refs = op.get("x-spec-refs") or []
        if not refs:
            error("TRACE-01", f"{where}: sin x-spec-refs")
        for ref in refs:
            if ref not in rf_ids | us_ids:
                error("TRACE-02", f"{where}: x-spec-refs '{ref}' no existe en specs/")
        covered |= set(refs)

        responses = op.get("responses", {})
        if "{productId}" in path and "404" not in responses:
            error("OP-04", f"{where}: tiene {{productId}} pero no declara 404")
        if "requestBody" in op and "400" not in responses:
            error("OP-05", f"{where}: recibe body pero no declara 400")
        for code, resp in responses.items():
            if code.startswith("4"):
                body = resolve(doc, resp["$ref"]) if "$ref" in resp else resp
                schema = body.get("content", {}).get("application/json", {}).get("schema", {})
                if schema.get("$ref") != "#/components/schemas/ErrorResponse":
                    error("ERR-01", f"{where}: la respuesta {code} no usa ErrorResponse")

        transition = op.get("x-transition")
        if transition:
            origin, target = [s.strip() for s in transition.split("->")]
            for o in origin.split("|"):
                pair = (None if o == "-" else o, target)
                for s in pair:
                    if s is not None and s not in states:
                        error("LIFE-01", f"{where}: x-transition usa el estado inexistente '{s}'")
                if pair not in valid_transitions:
                    error("LIFE-02", f"{where}: la transición {o} -> {target} no está en product-lifecycle.md")
                used_transitions.add(pair)
            if origin != "-" and "409" not in responses:
                error("LIFE-03", f"{where}: cambia de estado pero no declara 409")

    for missing in sorted(valid_transitions - used_transitions, key=str):
        error("LIFE-04", f"La transición {missing[0] or '-'} -> {missing[1]} de product-lifecycle.md "
                         f"no tiene operación en el contrato")
    for spec_id in sorted((rf_ids | us_ids) - covered):
        error("TRACE-03", f"{spec_id} no está cubierto por ninguna operación")
    return seen_ids


def check_domain(doc):
    schemas = doc["components"]["schemas"]
    pairs = [
        ("DOM-01", "ProductStatus", SPECS / "domain" / "product-lifecycle.md", "Código"),
        ("DOM-02", "ProductCategory", SPECS / "domain" / "catalogs.md", "Código"),
        ("DOM-03", "ErrorCode", SPECS / "adr" / "0004-codigos-de-error.md", "Código de error"),
    ]
    for rule, schema_name, md, header in pairs:
        expected = column(md, header)
        actual = schemas.get(schema_name, {}).get("enum")
        if expected is None:
            error(rule, f"No se encontró la columna '{header}' en {md.relative_to(ROOT)}")
        elif actual != expected:
            error(rule, f"{schema_name} del contrato {actual} != {md.relative_to(ROOT)} {expected}")


def check_java_enums(doc):
    schemas = doc["components"]["schemas"]
    java_root = ROOT / "src" / "main" / "java"
    for name in ("ProductStatus", "ProductCategory"):
        files = list(java_root.rglob(f"{name}.java")) if java_root.exists() else []
        if not files:
            warn("CODE-00", f"Aún no existe {name}.java (normal antes de implementar)")
            continue
        body = re.sub(r"/\*.*?\*/|//[^\n]*", "", files[0].read_text(encoding="utf-8"), flags=re.S)
        m = re.search(r"enum\s+\w+\s*\{(.*?)(;|\})", body, flags=re.S)
        values = [v.strip() for v in m.group(1).split(",") if v.strip()] if m else []
        if values != schemas[name]["enum"]:
            error("CODE-01", f"Enum Java {name} {values} != contrato {schemas[name]['enum']}")


def check_examples(doc):
    for where, node in walk(doc):
        if "schema" in node and ("example" in node or "examples" in node):
            samples = [("example", node["example"])] if "example" in node else \
                [(k, v.get("value")) for k, v in node["examples"].items()]
            for name, value in samples:
                errs = []
                validate(doc, node["schema"], value, name, errs)
                for e in dict.fromkeys(errs):
                    error("EX-01", f"Ejemplo inválido en {where}: {e}")


def main():
    try:
        doc = yaml.safe_load(CONTRACT.read_text(encoding="utf-8"))
    except (OSError, yaml.YAMLError) as e:
        print(f"[YAML-01] No se pudo leer el contrato: {e}")
        return 1
    if not str(doc.get("openapi", "")).startswith("3."):
        error("OAS-01", "El archivo no es OpenAPI 3.x")

    check_refs(doc)
    ops = check_operations(doc)
    check_domain(doc)
    check_java_enums(doc)
    check_examples(doc)

    print(f"Contrato: {CONTRACT.relative_to(ROOT)} (v{doc['info']['version']})")
    print(f"Operaciones: {len(ops)} | Errores: {len(errors)} | Advertencias: {len(warnings)}")
    for e in errors:
        print("  ERROR ", e)
    for w in warnings:
        print("  AVISO ", w)
    print("RESULTADO:", "OK - contrato listo para implementar" if not errors else "FALLO")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
