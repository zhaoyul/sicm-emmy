;; # Emmy 功能完全展示
;;
;; 本文档根据 `readme.org` 的内容生成，旨在完整展示 Emmy 计算机代数系统与 Clerk 集成的各项功能。

(ns emmy-clerk-demo.full-demo
  (:refer-clojure :exclude [+ - * / zero? compare divide numerator denominator
                            infinite? abs ref partial =])
  (:require [emmy.clerk :as ec]
            [emmy.env :as e :refer :all]
            [emmy.mafs :as mafs]
            [emmy.mathbox.plot :as plot]
            [emmy.leva :as leva]
            [emmy.viewer :as viewer]
            [nextjournal.clerk :as clerk]))

;; ## 1. 查看器安装
;;
;; 为了在 Clerk 中正确渲染 Emmy 的可视化组件，我们首先需要调用 `emmy.clerk/install!`。
;; 这会向 Clerk 注册 `emmy-viewers` 的自定义查看器。

^{::clerk/visibility {:code :hide :result :hide}}
(ec/install!)

;; ## 2. 基础：符号表达式
;;
;; Emmy 的核心是处理符号表达式的能力。我们可以定义复杂的表达式，然后对其进行化简或以不同格式渲染。

;; ### 符号化简
;;
;; `simplify` 函数可以将复杂的表达式归约为其最简形式。

;;; #### 勾股定理
;;;
;;; 著名的三角恒等式：
;;; $$\sin^{2}(x) + \cos^{2}(x) = 1$$

(def expr (e/+ (e/square (e/sin 'x))
               (e/square (e/cos 'x))))

(e/simplify expr)

;; ### 渲染为不同格式
;;
;; Emmy 表达式可以被渲染为人类可读的中缀字符串或高质量的 LaTeX。

;; 定义一个辅助函数，方便地将 Emmy 表达式渲染为 Clerk 的 TeX 公式。
(def tex (comp clerk/tex ->TeX))

(def formula (e/square (e/sin (e/+ 'x 'y))))

;;; #### 中缀表示
(->infix formula)

;;; #### LaTeX 表示
(tex formula)


;; ## 3. 微积分
;;
;; 微积分是 Emmy 的核心功能之一，尤其是在符号微分方面。

;; ### 自动与符号微分
;;
;; 强大的 `D` 算子可以对任何 Emmy 函数进行微分操作。

;;; #### 求 `x^3` 的导数
(simplify ((D cube) 'x))

;;; #### 泰勒级数
;;; `D` 算子甚至可以被指数化，用于生成泰勒级数展开。
;;; 下面是函数 `f(x)` 在 `x` 点的泰勒级数展开（取前5项）。
(let [f (literal-function 'f)]
  (series:sum
   (((exp D) f) 'x)
   5))

;; ## 4. 案例研究：中心力问题中的拉格朗日力学
;;
;; 这个例子将综合展示 Emmy 作为物理研究工具的强大表达力。
;; 我们将重现一个经典物理问题：在极坐标下求解中心力场的运动方程。

;; ### 第 1 步：定义拉格朗日量
;;
;; 拉格朗日量 $L = T - V$，其中 $T$ 是动能，$V$ 是势能。
;; 在极坐标 $(r, \theta)$ 中，一个质量为 $m$ 的粒子的动能是 $T = \frac{1}{2} m (\dot{r}^2 + (r\dot{\theta})^2)$。
;; 我们可以定义一个高阶函数来表示它，其中 `U` 是一个代表势能的抽象函数。

(defn L-central-polar [m U]
  (fn [[_ [r] [rdot thetadot]]]
    (e/- (e/* 1/2 m (e/+ (e/square rdot)
                          (e/square (e/* r thetadot))))
         (U r))))

;; ### 第 2 步：定义系统状态和运动方程
;;
;; 我们使用 `literal-function` 来定义广义坐标 $r(t)$ 和 $\theta(t)$。
;; `Lagrange-equations` 算子会自动推导出系统的欧拉-拉格朗日方程。
;;
;; $$ \frac{d}{dt} \frac{\partial L}{\partial \dot{q}} - \frac{\partial L}{\partial q} = 0 $$

(let [potential-fn (literal-function 'U)
      L (L-central-polar 'm potential-fn)
      state (up (literal-function 'r)
                (literal-function 'theta))
      equations (simplify
                 (((Lagrange-equations L) state) 't))]
  (tex equations))

;; ## 5. 交互式 2D 可视化
;;
;; `emmy-viewers` 库的 `emmy.mafs` 命名空间提供了强大的 2D 可视化能力。

;; ### 函数图像
;; 绘制一个简单的蓝色正弦函数。
(mafs/of-x sin {:color :blue})

;; ### 组合元素
;; 组合坐标系、余弦函数和一条线段。
;; (mafs/mafs
;;  {}
;;  (mafs/cartesian)
;;  (mafs/of-x cos {:color :indigo})
;;  ;; (mafs/lineSegment {:point1 [-2 -1] :point2 [2 1]}))


;; ### 不等式
;; 绘制不等式 $\sin(x) \le y < \cos(x)$ 所表示的区域。
(mafs/inequality {:y {:<= cos :> sin}})


;; ## 6. 3D 场景与参数曲线
;;
;; `emmy.mathbox.plot` 命名空间则负责 3D 场景的构建。

;; ### 3D 参数曲线
;; 绘制一条 3D 参数曲线 $(\sin(t), \cos(t), t/3)$。
(plot/scene
 (plot/parametric-curve
  {:f (up sin cos (e// identity 3))
   :t [-10 10]
   :color :green}))

;; ### 3D 曲面
;; 绘制曲面 $z = \sin(\sqrt{x^2 + y^2})$。
;; (plot/scene
;;  (plot/surface
;;   {:z (comp e/sin e/sqrt e/+ (e/square e/identity) (e/square e/identity))
;;    :x [-5 5]
;;    :y [-5 5]}))


;; ## 7. 交互式探索：可塑造的微世界
;;
;; `emmy.leva` 允许我们创建 UI 控件（如滑块），并将它们的值绑定到 Clojure 的 atom 上，
;; 从而实现对数学模型的实时交互式探索。

;; ### 交互式 2D 函数
;;
;; 创建一个滑块来控制正弦函数的频率 `f` 和振幅 `a`。
;; (viewer/with-let [!bindings (leva/controls {:f {:min 1 :max 5 :step 0.1}
;;                                             :a {:min 0 :max 2 :step 0.1}})]
;;   (mafs/mafs
;;    (mafs/cartesian)
;;    (mafs/of-x
;;     (e/fn [x]
;;       (e/* (e/sin (e/* (:f !bindings) x))
;;            (:a !bindings))))))

;; ### 交互式 3D 曲面
;;
;; 创建一个滑块来控制 3D 曲面的相位 `phase`。
;; (viewer/with-let [!bindings (leva/controls {:phase {:min 0 :max (* 2 Math/PI)}})]
;;   (plot/scene
;;    (plot/surface
;;     {:z (e/fn [x y]
;;           (e/sin
;;            (e/- (e/sqrt (e/+ (e/square x) (e/square y)))
;;                 (:phase !bindings))))
;;      :x [-10 10]
;;      :y [-10 10]})))

;; ---
;; 文档结束
