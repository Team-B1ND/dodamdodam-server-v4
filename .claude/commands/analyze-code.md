---
description: Draw method pipeline and relationship diagrams for a given class
argument-hint: <class-name> [method-name]
---

# Diagram Pipeline

When this command runs, you MUST first determine two inputs:

1. **Which class?** — required. Ask the user if not provided in `$ARGUMENTS`.
2. **Which method?** — optional. May be `null`, meaning no method was specified.

## Inputs

- Class name: `$1`
- Method name: `$2` (may be empty / null)

## Behavior

### Case A — A method IS specified (`$2` is not null)

Draw **only** the pipeline for that single method:

- Trace the method's internal flow step by step (inputs → processing stages → outputs).
- Show calls it makes to other methods or external dependencies.
- Render as a clear top-to-bottom pipeline diagram.

### Case B — NO method specified (`$2` is null / empty)

Do BOTH of the following:

1. **Per-method pipelines** — for each method in the class, draw its individual pipeline (inputs → stages → outputs).
2. **Inter-method relationship map** — draw a single diagram showing how the methods relate to and call one another within the class.

## Steps

1. If `$1` (class) is missing, ask: "Which class?"
2. Locate the class definition in the codebase.
3. Determine whether `$2` (method) was provided.
4. Execute Case A or Case B accordingly.
5. Output diagrams using Mermaid (`flowchart` / `graph`) fenced code blocks.

## Output format

- **All explanations, headings, and descriptions MUST be written in Korean (한국어).**
- Use Mermaid for all diagrams.
- Label nodes with real method/stage names from the code.
- Keep one diagram per fenced block; add a short heading (in Korean) above each.