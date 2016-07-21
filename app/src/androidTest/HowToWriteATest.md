[Wiki Entry](https://github.com/wearezeta/zclient-android/wiki/Mocked-Tests)

#### Writing a test
To set up a test for a Fragment, extend the FragmentTest class (which sets up a blank test activity for you) and then start writing your tests.

Directly after the blank test activity is created, it's Store and ControllerFactories are set up to return mocks whenever we ask for one of the Stores or Controllers. We can then force individual methods from these mock objects to return values using `Mockito.when().thenReturn();`
```
IAppEntryStore mockAppEntryStore = activity.getStoreFactory().getAppEntryStore();
when(mockAppEntryStore.getName()).thenReturn("Jake the Dog");
```

**Note:** you don't have to mock out every single method that your test component uses. The way the mock Stores and Controllers are set up, any calls to their methods will just fall through to empty implementations of that Store or Controller, so the component can continue to function.

If you want to check that a given method was called from one of the Stores or Controllers, then use the Mockito.verify() method:

```
verify(activity.getStoreFactory().getAppEntryStore()).signInWithEmail(eq(email), eq(password), any(IAppEntryStore.ErrorCallback.class));
```

For a working example, checkout the EmailSignInFragment class.

In order to write nice clean tests, be sure to look at how to use [Mockito](http://mockito.org/) and [Espresso](https://google.github.io/android-testing-support-library/docs/espresso/index.html). As we write more tests, I can start pulling out more common testutils stuff to make them tidier and less flaky, just ask me for help :)

#### Running the tests

For the tests to run, there are two APKs that get installed on the device, the actual app APK and a test APK. The best way to run the tests is to first install the app APK: (installDevDebug)

```
./gradlew iDD
```

And then to (uninstall and) install the test APK: (uninstallDevDebugAndroidTest, installDevDebugAndroidTest)

```
./gradlew uDDAT iDDAT
```

Once the test APK is installed, you can run the tests using:

```
adb shell am instrument -w -r com.waz.zclient.dev.test/android.support.test.runner.AndroidJUnitRunner
```

**Note:** building the test APK does NOT build the app APK. This means that you can build the test APK in about 30 seconds if you just make changes to the tests. I don't use the `connectedXAndroidTest` because it builds everything, is hard to control and then deletes both APKs once it's finished, which can be really annoying.


#### Common Issues:
##### Bad Service Configuration File
If you get an error that looks like:

```
error: Bad service configuration file, or exception thrown while constructing Processor object: javax.annotation.processing.Processor: Error reading configuration file
```

Then run `./gradlew --stop` and try building again. This sometimes happens if there was an issue building last time, but once the annotations processing stuff works, this shouldn't happen again.

##### Can't find StubXyzStore
This will happen if you haven't touched any code in the `wire-core` module; the core won't rebuild, meaning that all of the stub stores will not get generated. To get around this, just build with the `--rerun-tasks` flag in gradle, and you should have them again.