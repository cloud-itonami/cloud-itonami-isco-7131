(ns paintcrew.store
  "SSoT for the ISCO-08 7131 painting-crew job-site scheduling/logistics
  coordination actor (itonami actor pattern, ADR-2607121000 / CLAUDE.md
  Actors section; README's 'Robotics premise' — a job-site
  scheduling/logistics coordination robot performs crew scheduling,
  task/materials-usage/progress-record logging and paint/coating-materials
  supply-order coordination for a painting crew under this
  advisor/governor pair, which never dispatches hardware itself, never
  performs painting work itself, and never finalizes a
  painting-execution decision (e.g. a specific coating or finish
  application) or overrides a site safety officer's judgment — those
  remain the site safety officer's exclusive judgment). Modeled on
  cloud-itonami-isco-7111's housebuilder.store (and closely on
  cloud-itonami-isco-9311's mininglabor.store for the
  physical-safety-domain shape).

  Domain:

    painter — a registered painting-crew member
              (:painter-id, :name)
    site    — a registered painting job site {:site-id :name
              :max-supply-cost number}. `:max-supply-cost` is an
              informational registered ceiling used only to decide
              whether a `:coordinate-supply-order` proposal escalates
              to human sign-off (the governor never blocks a
              within-threshold order outright; it only decides
              commit vs. escalate).
    record  — a committed operating record (a logged
              task/materials-usage/progress entry, a scheduled crew
              operation, a flagged safety concern, or a coordinated
              supply order) — written ONLY via commit-record!.
    ledger  — append-only audit trail, commit or hold.")

(defprotocol Store
  (painter [s painter-id])
  (site [s site-id])
  (records-of [s painter-id])
  (ledger [s])
  (register-painter! [s painter])
  (register-site! [s site])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (painter [_ painter-id] (get-in @a [:painters painter-id]))
  (site [_ site-id] (get-in @a [:sites site-id]))
  (records-of [_ painter-id] (filter #(= painter-id (:painter-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-painter! [s p]
    (swap! a assoc-in [:painters (:painter-id p)] p) s)
  (register-site! [s st]
    (swap! a assoc-in [:sites (:site-id st)] st) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:painters {} :sites {} :records [] :ledger []}
                                    seed)))))
