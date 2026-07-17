(ns paintcrew.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [paintcrew.actor :as actor]
            [paintcrew.store :as store]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-painter! st {:painter-id "painter-1" :name "Kobo Yamada"})
    (store/register-site! st {:site-id "S-1" :name "Kobo Paint Site" :max-supply-cost 2000})
    st))

(deftest commits-a-registered-work-log
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:painter-id "painter-1" :op :log-work-record :stake :low
                  :site-id "S-1" :task "interior wall coating progress log"}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (some? (get-in result [:state :record])))
    (is (= 1 (count (store/records-of st "painter-1"))))))

(deftest holds-an-unregistered-site-proposal
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:painter-id "painter-1" :op :log-work-record :stake :low
                  :site-id "S-ghost" :task "interior wall coating progress log"}
        result (actor/run-request! graph request {} "thread-2")]
    (is (= :hold (:disposition (:state result))))
    (is (empty? (store/records-of st "painter-1")))))

(deftest interrupts-then-approves-safety-concern-on-human-approval
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:painter-id "painter-1" :op :flag-safety-concern :stake :low
                  :site-id "S-1" :hazard-type :fume-exposure-risk}
        interrupted (actor/run-request! graph request {} "thread-3")]
    (is (= :interrupted (:status interrupted)))
    (is (empty? (store/records-of st "painter-1")))
    (let [resumed (actor/approve! graph "thread-3")]
      (is (= :done (:status resumed)))
      (is (= 1 (count (store/records-of st "painter-1")))))))

(deftest holds-a-scope-excluded-op-even-at-high-confidence
  (testing "an actor run can never commit a proposal that would finalize a painting-execution decision, regardless of disposition path"
    (let [st (fresh-store)
          graph (actor/build-graph {:store st})
          request {:painter-id "painter-1" :op :finalize-painting-decision :stake :low
                    :site-id "S-1" :task "painting decision"}
          result (actor/run-request! graph request {} "thread-4")]
      (is (= :done (:status result)))
      (is (= :hold (:disposition (:state result))))
      (is (empty? (store/records-of st "painter-1"))))))
