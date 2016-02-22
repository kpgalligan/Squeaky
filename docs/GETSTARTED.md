# Getting Started

Add the apt dependency to the main build.gradle

```gradle
classpath 'com.neenbedankt.gradle.plugins:android-apt:1.4'
```

Add plugin to the app build.gradle

```gradle
apply plugin: 'com.neenbedankt.android-apt'
```

Add dependencies to the app build.gradle

```gradle
apt 'co.touchlab.squeaky:squeaky-processor:0.5.0.0'
compile 'co.touchlab.squeaky:squeaky-query:0.5.0.0'
```

In your model classes, add annotations.

On the top of the class, add @DatabaseTable.  It will default to naming the table the same as the class.

For each field in the database, add the @DatabaseField annotation.

To access data