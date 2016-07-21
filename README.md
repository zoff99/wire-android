# Wire

This repository is part of the source code of Wire. You can find more information at [wire.com](https://wire.com) or by contacting opensource@wire.com.

You can find the published source code at [github.com/wireapp](https://github.com/wireapp). 

For licensing information, see the attached LICENSE file and the list of third-party licenses at [wire.com/legal/licenses/](https://wire.com/legal/licenses/).

## Contributions

Wire is in the process of releasing source code, but is not yet actively developing in all open source repositories. For this reason, we are not able to accept all pull requests at the moment.


# Wire for Android

This repository contains sources of Wire for Android app.

## Building

App can be build using Gradle. You may need to run following command twice:

```
./gradlew assembleProdRelease
```

## Dependencies

Several dependencies are configured in build scripts, their binary releases are used for building.
User might want to build some of them from source, here are the most interesting ones:

- [zmessaging-android](https://github.com/wireapp/zmessaging-android)
- [avs](https://github.com/wireapp/avs)
- [wiretranslations-android](https://github.com/wireapp/wiretranslations-android)

