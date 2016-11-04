(defn f [func]
  (fn [& arguments]
    (fn [args]
      (apply func (map (fn [a] (a args)) arguments)))))

(def add (f +))
(def subtract (f -))
(def multiply (f *))
(def divide (f (fn [a b] (/ (double a) b))))
(defn constant [x]
  (fn [args] x))

(defn variable [x]
  (fn [args] (.get args x)))

(defn negate [x]
  (fn [args] (- (x args))))



(defn evaluate [x vars] (
                          (.get (.get x :proto) :evaluate) vars))

(defn diff [x diffVariable] (
                              (.get (.get x :proto) :diff) (.get x :arguments) diffVariable))

(defn toString [x] (
                          (.get (.get x :proto) :toString)
                          ))

(defn Constant [x] {
                    :arguments x
                    :proto {
                            :evaluate (fn [vars] x)
                            :diff (fn [a diffVariable] (Constant 0))
                            :toString (fn [] (str x))
                            }
                    })

(defn Variable [x] {
                    :arguments x
                    :proto     {
                                :evaluate (fn [vars] (.get vars x))
                                :diff     (fn [a diffVariable] (if (= x diffVariable)
                                                                 (Constant 1)
                                                                 (Constant 0)))
                                :toString (fn [] x)
                                }
                    })

(defn constructor [func sign dif]
  (fn [& args] {
                 :arguments args
                 :proto     {
                             :evaluate (fn [vars] (apply func (map (fn [x] (evaluate x vars)) args)))
                             :diff     dif
                             :toString (fn [] (clojure.string/join "" ["(" sign " " (clojure.string/join " " (map toString args)) ")"]))
                             }
                 }
                 ))


(def Add (constructor + "+" (fn [a diffVariable] (apply Add (map (fn [x] (diff x diffVariable)) a)))))
(def Subtract (constructor - "-" (fn [a diffVariable] (apply Subtract (map (fn [x] (diff x diffVariable)) a)))))
(def Multiply (constructor * "*" (fn [a diffVariable]
                                                        (if  (= (.size a) 1) (diff (first a) diffVariable)
                                                                            (do
                                                                              (def firstarg (first a))
                                                                              (def secondarg (apply Multiply (rest a)))
                                                                              (Add
                                                                                     (Multiply (diff firstarg diffVariable) secondarg)
                                                                                     (Multiply firstarg (diff secondarg diffVariable)))))
                                                                            )))

(def Divide (constructor / "/"  (fn [a diffVariable] (apply (fn [a b] (
                                                        Divide
                                                        (Subtract
                                                          (Multiply (diff a diffVariable) b)
                                                          (Multiply a (diff b diffVariable)))
                                                        (Multiply b b)
                                                        )) a))))

(def Negate (constructor (fn [x] (- x)) "negate" (fn [a diffVariable] (
                                                                Negate (diff (first a) diffVariable)
                                                                ))))


(declare Cos)
(def Sin (constructor (fn [x] (Math/sin x)) "sin" (fn [a diffVariable] (
                                                                             Multiply(diff (first a) diffVariable)(Cos ( first a
                                                                                                                            ))))))

(def Cos (constructor (fn [x] (Math/cos x)) "cos" (fn [a diffVariable] (
                                                                         Negate (Multiply (diff (first a) diffVariable) (Sin (first a)))))))




(def listofoperands {'+ Add
                     '- Subtract
                     '* Multiply
                     '/ Divide
                     'negate Negate
                     'cos Cos
                     'sin Sin
                     })

(defn parseObject2 [list]
  (if (not= (type list) clojure.lang.PersistentList)
    (if (= (type list) clojure.lang.Symbol)
      (Variable (str list))
      (Constant list))
    (apply (.get listofoperands (.peek list)) (map parseObject2 (pop list)))))

(defn parseObject [str] (parseObject2 (read-string str)))

(def expr
  (Subtract
    (Multiply
      (Constant 2)
      (Variable "x"))
    (Constant 3)))

(println (toString expr))
