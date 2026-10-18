#!/usr/bin/env python3
"""
spec_sync.py - Sincronización specs <-> implementación del Catálogo de Productos.

Complementa a spec_lint.py: spec_lint valida el contrato ANTES de implementar; spec_sync verifica
DESPUÉS de implementar que el código, las pruebas y la documentación siguen diciendo lo mismo que specs/.

Uso:
    python tools/spec_sync.py            # informe en consola
    python tools/spec_sync.py --md FILE  # además escribe el informe en markdown
Salida: código 0 si no hay errores, 1 si hay errores (las advertencias no fallan).
Requiere: pip install pyyaml
"""
import re
import sys
from pathlib import Path

import yaml

try:
    sys.stdout.reconfigure(encoding="utf-8")  # consola de Windows
except AttributeError:
    pass

ROOT = Path(__file__).resolve().parent.parent
SPECS = ROOT / "specs"
MAIN = ROOT / "src" / "main" / "java"
TEST = ROOT / "src" / "test" / "java"
CONTRACT = SPECS / "api" / "catalog-openapi.yaml"

# Tipos del JDK o de librerías que aparecen en las specs y no son clases del proyecto.
EXTERNAL_TYPES = {
    "Clock", "UUID", "Optional", "BigDecimal", "Instant", "List", "String", "Product", "ProductsApi",
    "ErrorResponse", "JsonSetter", "Nulls", "AccessDeniedException", "JpaRepository",
}

# Dependencia del pom.xml -> texto que debe aparecer en tech-stack.md.
POM_TO_STACK = {
    "spring-boot-starter-webmvc": "Web",
    "spring-boot-starter-validation": "Validation",
    "spring-boot-starter-data-jpa": "JPA",
    "h2": "H2",
    "springdoc-openapi-starter-webmvc-ui": "springdoc",
    "openapi-generator-maven-plugin": "openapi-generator",
    "archunit": "ArchUnit",
    "archunit-junit5": "ArchUnit",
    "jacoco-maven-plugin": "JaCoCo",
    "spring-boot-starter-actuator": "Actuator",
    "spring-boot-starter-security": "Security",
}

errors, warnings = [], []


def error(rule, msg):
    errors.append((rule, msg))


def warn(rule, msg):
    warnings.append((rule, msg))


# ------------------------------------------------------------------ lectura
def read(path):
    return path.read_text(encoding="utf-8", errors="replace")


def md_tables(path):
    """Tablas del markdown como lista de (encabezados, filas)."""
    tables, current = [], None
    for line in read(path).splitlines():
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


def java_files(base):
    return {p.stem: p for p in base.rglob("*.java")} if base.exists() else {}


def strip_code(cell):
    return cell.replace("`", "").strip()


# ------------------------------------------------------------------ reglas
def check_operations(main):
    """SYNC-01: cada operationId del contrato está implementado en ProductController."""
    doc = yaml.safe_load(read(CONTRACT))
    op_ids = [op["operationId"] for item in doc.get("paths", {}).values()
              for m, op in item.items() if isinstance(op, dict) and "operationId" in op]
    ctrl = main.get("ProductController")
    if not ctrl:
        error("SYNC-01", "No existe ProductController.java")
        return op_ids
    src = read(ctrl)
    for oid in op_ids:
        m = re.search(rf"\b{oid}\s*\(", src)
        if not m:
            error("SYNC-01", f"{oid}: no está implementado en ProductController")
            continue
        body = src[m.end():]
        nxt = re.search(r"\n\t@Override|\n    @Override", body)
        body = body[: nxt.start()] if nxt else body
        if "UnsupportedOperationException" in body or "TODO" in body:
            error("SYNC-01", f"{oid}: sigue como TODO / UnsupportedOperationException")
    return op_ids


def check_traceability(main, test, op_ids):
    """SYNC-02/03: casos de uso, servicios y clases de prueba de traceability.md existen."""
    path = SPECS / "traceability.md"
    seen_ops = set()
    for headers, rows in md_tables(path):
        h = [x.lower() for x in headers]
        if "operationid" not in h:
            continue
        for row in rows:
            cells = dict(zip(h, row))
            oid = strip_code(cells.get("operationid", ""))
            seen_ops.add(oid)
            uc = strip_code(cells.get("caso de uso (port.in)", ""))
            svc = strip_code(cells.get("servicio", ""))
            if uc and uc not in main:
                error("SYNC-02", f"traceability.md ({oid}): no existe {uc}.java")
            if svc:
                if svc not in main:
                    error("SYNC-02", f"traceability.md ({oid}): no existe {svc}.java")
                elif uc and not re.search(rf"implements[^{{]*\b{uc}\b", read(main[svc])):
                    error("SYNC-02", f"{svc} no implementa {uc}")
            for key, value in cells.items():
                if key in ("operationid", "caso de uso (port.in)", "servicio", "rf / us",
                           "invariantes", "pruebas mínimas"):
                    continue
                for name in re.findall(r"\b([A-Z]\w*Test)\b", value):
                    if name not in test:
                        error("SYNC-03", f"traceability.md ({oid}): no existe la clase de prueba {name}")
    for oid in op_ids:
        if oid not in seen_ops:
            error("SYNC-02", f"{oid} está en el contrato pero no en traceability.md")
    # Pruebas de reglas transversales
    for headers, rows in md_tables(path):
        if [x.lower() for x in headers][:2] == ["regla", "clase de prueba"]:
            for row in rows:
                for name in re.findall(r"\b([A-Z]\w*Test)\b", row[1]):
                    if name not in test:
                        error("SYNC-03", f"traceability.md (regla '{row[0][:40]}'): no existe {name}")


def check_acceptance_tests(test):
    """SYNC-04/05: cada criterio de aceptación tiene prueba y no hay pruebas huérfanas."""
    criteria = set()
    for story in sorted((SPECS / "user-stories").glob("US*.md")):
        us = re.match(r"US(\d+)", story.name).group(1)
        for ca in re.findall(r"\*\*CA-(\d+):\*\*", read(story)):
            criteria.add(f"US{us}_CA{ca}")
    methods = {}
    for path in test.values():
        for name in re.findall(r"void\s+(US\d+_CA\d+)_\w+\s*\(", read(path)):
            methods.setdefault(name, set()).add(path.stem)
    for ca in sorted(criteria):
        if ca not in methods:
            error("SYNC-04", f"{ca}: criterio de aceptación sin ninguna prueba")
    for name in sorted(set(methods) - criteria):
        error("SYNC-05", f"{name}: prueba que cita un criterio inexistente ({', '.join(sorted(methods[name]))})")
    return criteria, methods


def check_invariants():
    """SYNC-06: invariantes de domain-model.md y traceability.md coinciden."""
    model = set(re.findall(r"\|\s*(INV-\d+)\s*\|", read(SPECS / "architecture" / "domain-model.md")))
    trace = set(re.findall(r"INV-\d+", read(SPECS / "traceability.md")))
    for inv in sorted(model - trace):
        warn("SYNC-06", f"{inv} está en domain-model.md pero ninguna operación lo cita en traceability.md")
    for inv in sorted(trace - model):
        error("SYNC-06", f"{inv} se cita en traceability.md pero no existe en domain-model.md")


def check_adrs():
    """SYNC-07: índice de ADR completo y referencias ADR-NNNN válidas en specs y código."""
    adr_dir = SPECS / "adr"
    files = {re.match(r"(\d{4})", p.name).group(1) for p in adr_dir.glob("[0-9][0-9][0-9][0-9]-*.md")}
    files.discard("0000")
    index = set(re.findall(r"\[(\d{4})\]", read(adr_dir / "README.md")))
    for n in sorted(files - index):
        error("SYNC-07", f"ADR-{n} existe pero no está en specs/adr/README.md")
    for n in sorted(index - files):
        error("SYNC-07", f"specs/adr/README.md lista ADR-{n}, pero el archivo no existe")
    sources = list(SPECS.rglob("*.md")) + list(MAIN.rglob("*.java")) + list(TEST.rglob("*.java"))
    sources += [ROOT / "CLAUDE.md"] if (ROOT / "CLAUDE.md").exists() else []
    for path in sources:
        for n in sorted(set(re.findall(r"ADR-(\d{4})", read(path)))):
            if n not in files and n != "0000":
                error("SYNC-07", f"{path.relative_to(ROOT)} cita ADR-{n}, que no existe")


def check_architecture_types(main):
    """SYNC-08: los tipos que nombra architecture.md existen; SYNC-09: clases sin documentar."""
    text = read(SPECS / "architecture" / "architecture.md")
    generated = set(yaml.safe_load(read(CONTRACT)).get("components", {}).get("schemas", {}))
    named = set(re.findall(r"\b([A-Z][a-z]+(?:[A-Z][a-z0-9]*)+)\b", text))
    for name in sorted(named):
        if name in EXTERNAL_TYPES or name in generated or name in main:
            continue
        if re.search(r"(UseCase|Service|Command|Query|Result|Port|Page|Adapter|Mapper|Entity|"
                     r"Exception|Controller|Configuration|Jpa|Handler)$", name):
            error("SYNC-08", f"architecture.md nombra {name}, pero no existe en src/main")
    spec_text = "\n".join(read(p) for p in SPECS.rglob("*.md"))
    for name in sorted(main):
        if "generated" in main[name].parts or name.endswith("Application"):
            continue
        if name not in spec_text:
            warn("SYNC-09", f"{main[name].relative_to(ROOT)}: clase no mencionada en ninguna spec")


def check_stack():
    """SYNC-10: dependencias del pom.xml documentadas en tech-stack.md (cambio de stack = ADR)."""
    pom = read(ROOT / "pom.xml")
    stack = read(SPECS / "architecture" / "tech-stack.md").lower()
    for artifact in re.findall(r"<artifactId>([^<]+)</artifactId>", pom):
        keyword = POM_TO_STACK.get(artifact.strip())
        if keyword and keyword.lower() not in stack:
            error("SYNC-10", f"pom.xml usa {artifact}, pero tech-stack.md no menciona '{keyword}'")
    for keyword in ("ArchUnit", "JaCoCo"):
        if keyword.lower() in stack and keyword.lower() not in pom.lower():
            warn("SYNC-10", f"tech-stack.md menciona {keyword}, pero no está en pom.xml")


def check_status_values(main):
    """SYNC-11: estados del enum Java == product-lifecycle.md (mismo orden)."""
    lifecycle = []
    for headers, rows in md_tables(SPECS / "domain" / "product-lifecycle.md"):
        if "Código" in headers:
            col = headers.index("Código")
            lifecycle = [strip_code(r[col]) for r in rows]
            break
    enum = main.get("ProductStatus")
    if enum:
        body = read(enum).split("{", 1)[1]
        values = re.findall(r"\b([A-Z][A-Z_]+)\b\s*[,;(]?", body.split(";")[0])
        if values != lifecycle:
            error("SYNC-11", f"ProductStatus {values} != product-lifecycle.md {lifecycle}")


# ------------------------------------------------------------------ informe
def report_md(path, criteria, methods):
    lines = ["# Informe de sincronización specs ↔ implementación", "",
             "Generado por `python tools/spec_sync.py --md`. No editar a mano.", "",
             f"**Resultado:** {'OK' if not errors else 'CON ERRORES'} — "
             f"errores: {len(errors)}, advertencias: {len(warnings)}", ""]
    if errors or warnings:
        lines += ["| Tipo | Regla | Hallazgo |", "| :--- | :--- | :--- |"]
        lines += [f"| Error | {r} | {m} |" for r, m in errors]
        lines += [f"| Advertencia | {r} | {m} |" for r, m in warnings]
        lines.append("")
    lines += ["## Cobertura de criterios de aceptación", "",
              "| Criterio | Pruebas | Clases |", "| :--- | :---: | :--- |"]
    for ca in sorted(criteria):
        classes = sorted({c for name, cs in methods.items() if name == ca for c in cs})
        count = sum(len(re.findall(rf"void\s+{ca}_", read(p))) for p in TEST.rglob("*.java"))
        lines.append(f"| {ca} | {count} | {', '.join(classes) or '—'} |")
    Path(path).write_text("\n".join(lines) + "\n", encoding="utf-8")


def main():
    main_types, test_types = java_files(MAIN), java_files(TEST)
    op_ids = check_operations(main_types)
    check_traceability(main_types, test_types, op_ids)
    criteria, methods = check_acceptance_tests(test_types)
    check_invariants()
    check_adrs()
    check_architecture_types(main_types)
    check_stack()
    check_status_values(main_types)

    total = sum(len(re.findall(r"void\s+US\d+_CA\d+_", read(p))) for p in test_types.values())
    print(f"Operaciones: {len(op_ids)} | Criterios de aceptación: {len(criteria)} | Pruebas USxx_CAyy: {total}")
    for rule, msg in errors:
        print(f"ERROR [{rule}] {msg}")
    for rule, msg in warnings:
        print(f"AVISO [{rule}] {msg}")
    print(f"Errores: {len(errors)} | Advertencias: {len(warnings)}")
    print("RESULTADO:", "OK - specs e implementación sincronizadas" if not errors
          else "ERROR - specs e implementación divergen")
    if "--md" in sys.argv:
        report_md(sys.argv[sys.argv.index("--md") + 1], criteria, methods)
    sys.exit(1 if errors else 0)


if __name__ == "__main__":
    main()
