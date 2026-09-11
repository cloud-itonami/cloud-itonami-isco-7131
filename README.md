# cloud-itonami-isco-7131

Open Occupation Blueprint for **ISCO-08 7131**: Painters and Related Workers.

This repository designs a forkable OSS business for a painting-crew job-site scheduling and logistics coordination practice: a job-site scheduling and supply-coordination robot manages crew/task records under a governor-gated actor, so a painting crew keeps its own operating records instead of renting a closed workforce-management SaaS.

**Maturity: `:implemented`.** `src/paintcrew/` implements the
`PaintCrewActor` as a `langgraph.graph/state-graph`
(`paintcrew.actor`) wired to a `Paint Crew Advisor`
(`paintcrew.advisor`) and an independent `PaintCrewGovernor`
(`paintcrew.governor`), following the itonami actor pattern
(ADR-2607121000): `:intake -> :advise -> :govern -> :decide -+-> :commit
(:ok?) +-> :request-approval (:escalate?, human-in-the-loop interrupt)
+-> :hold (:hard?)`. 21 tests / 45 assertions green (`kbb -M:test`).
HARD invariants (always hold, never overridable): painter provenance,
site provenance, no-actuation (`:effect` must be `:propose`), a closed
op-allowlist (`:log-work-record`, `:schedule-crew-operation`,
`:flag-safety-concern`, `:coordinate-supply-order` — nothing else may
ever be proposed), and a permanent, unconditional block on any
proposal that would directly finalize a painting-execution decision
(e.g. a specific coating or finish application) or override a site
safety officer's judgment. Always-escalate paths (human sign-off
regardless of confidence, mapping this repo's Trust Controls in
[`docs/business-model.md`](docs/business-model.md)):
`:flag-safety-concern` (always) and `:coordinate-supply-order` above
the registered cost threshold.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a job-site scheduling/logistics coordination robot performs crew scheduling, task/materials-usage/progress-record logging and paint/coating-materials supply-order coordination for a painting crew, under an actor that proposes actions and an independent **Paint Crew Governor** that gates them. The governor never
dispatches hardware itself, never performs painting work on the job site, and never finalizes a painting-execution decision or overrides a site safety officer's judgment; `:high`/`:safety-critical` actions (such as a flagged fume-exposure/ventilation/height-work concern, or an above-threshold supply order) require human sign-off. **This actor coordinates job-site scheduling/logistics only — it never performs painting work itself.**

## Core Contract

```text
crew roster + job-site registration + safety-reporting policy
        |
        v
Paint Crew Advisor -> Paint Crew Governor -> log/schedule/coordinate, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, finalize
a painting-execution decision, override a site safety officer's judgment,
suppress an operating record, or disclose sensitive data without governor
approval and audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `7131`). Required capabilities:

- :robotics
- :identity
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
