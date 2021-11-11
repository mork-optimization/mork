# Common problems

Explanation of different problems, errors and exceptions that may be found while using the framework.

## IllegalAccessException / InaccessibleObjectException

Symptom: an exception such us
```
org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'patchMathRandom': Invocation of init method failed; nested exception is java.lang.reflect.InaccessibleObjectException: Unable to make field static final java.util.Random java.lang.Math$RandomNumberGeneratorHolder.randomNumberGenerator accessible: module java.base does not "opens java.lang" to unnamed module @631330c
[....]
Caused by: java.lang.reflect.InaccessibleObjectException: Unable to make field static final java.util.Random java.lang.Math$RandomNumberGeneratorHolder.randomNumberGenerator accessible: module java.base does not "opens java.lang" to unnamed module @631330c
```

Cause: Modern Java VMs do not allow to access certain classes and properties by default. This is only needed to patch or block certain functions such as Collections.shuffle or Math.random. The are two alternatives to solve the problem, depending on your needs

Fix 1: We want the patches --> Add the following parameters to the JVM launch options, to allow the patches to work:
```
--add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED
```

Fix 2: We do not need the patches --> Disable patching inside application.yml, section advanced.patches:
```
# Advanced configuration, do not change unless you know what you are doing!
advanced:
  # Block Java API methods
  block:
    # Collections.shuffle(RandomManager.getRandom()) should be used instead of Collections.shuffle()
    block-collections-shuffle: false
    # Block Math.random(), use RandomManager.getRandom().nextDouble()
    block-math-random: false
```

## The algorithm X does not have public constructors
Algorithms that do not have public constructors use [the builder pattern](https://stackoverflow.com/questions/328496/when-would-you-use-the-builder-pattern). Use the static method, example: `SimulatedAnnealing.builder()`.
