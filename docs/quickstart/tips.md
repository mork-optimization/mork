# Tips & Tricks


## Managing Java installations

Manually installation, updating, and removing old Java versions can get cumbersome. Managing Java versions, 
or more in general, Software Development Kits, can be simplified using a tool like [SdkMan](https://sdkman.io/install).

For example, listing available Java SDKs is as simple as:
```
sdk list java
================================================================================
Available Java Versions for Linux 64bit
================================================================================
 Vendor        | Use | Version      | Dist    | Status     | Identifier
--------------------------------------------------------------------------------
 Corretto      |     | 19.0.1       | amzn    |            | 19.0.1-amzn
               |     | 17.0.5       | amzn    |            | 17.0.5-amzn
               |     | 11.0.17      | amzn    |            | 11.0.17-amzn
               |     | 8.0.352      | amzn    |            | 8.0.352-amzn
 Dragonwell    |     | 17.0.5       | albba   |            | 17.0.5-albba
               |     | 17.0.4       | albba   |            | 17.0.4-albba
               |     | 11.0.17      | albba   |            | 11.0.17-albba
               |     | 11.0.16      | albba   |            | 11.0.16-albba
 Gluon         |     | 22.1.0.1.r17 | gln     |            | 22.1.0.1.r17-gln
               |     | 22.1.0.1.r11 | gln     |            | 22.1.0.1.r11-gln
 GraalVM       |     | 22.3.r19     | grl     |            | 22.3.r19-grl
               |     | 22.3.r17     | grl     |            | 22.3.r17-grl
               |     | 22.3.r11     | grl     |            | 22.3.r11-grl
[...]
```

And installing it:
```
sdk install java 19.0.1-tem

Downloading: java 19.0.1-tem

In progress...

################################################################################################################################################################################################################################# 100.0%

Repackaging Java 19.0.1-tem...

Done repackaging...

Installing: java 19.0.1-tem
Done installing!

Do you want java 19.0.1-tem to be set as default? (Y/n): n
```

!!! note

    Setting a Java SDK as default during installation with SDKMan may only affect new shells, 
    so you may need to close and open a new one. Moreover, you may need to manually choose in IntelliJ
    for each project which Java version should be used.


For a more detailed explanation, and full usage details see the official [SdkMan documentation](https://sdkman.io/usage). 