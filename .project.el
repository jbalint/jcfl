(:type :standard
 :source "1.8"
 :target "1.8"
 :options "-warn:+over-ann,uselessTypeCheck -warn:-enumSwitchPedantic,switchDefault,enumSwitch -proceedOnError -maxProblems 100"

 ;; source directory -> class directory mappings
 :paths (("src/main/java" . "build/classes/main")
         ("src/test/java" . "build/classes/test"))

 ;; library paths to check
 :lib-paths ("../connector-j-jardeps" "../connector-j-jardeps/6.0"))
