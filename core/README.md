# ORM for Android

Squeaky is yet another ORM for Android, but its awesome, so keep reading.

## Origins

Squeaky is based off of ORMLite, which has been a widely used ORM for Android since 2010 or so.  ORMLite was designed to be a minimal ORM, for developers who didn't need the excess of Hibernate (etc).  This mapped well to mobile, as the ~~excessive~~extensive feature set of server-class ORMs were neither needed nor desired.

However, ORMLite was designed to be used across many database systems, which means it has to be pretty generic and has lots of extra glue code.  Also, it uses reflection for copying data, which impacts performance.  It works well, but could work much better.

Squeaky maintains the general design of ORMLite, but is specific to SQLite on Android, which allows for simpler code and optimizations, and uses a20 (annotation processing) for source code generation instead of reflection.  Expect 2x performance in general, not to mention significantly less object creation.

## Features

### Direct object model usage

Several other frameworks generate your model code from definitions, so you can't actually add logic to your models.  Its not "your" code.  Squeaky generates the logic to communication with the database from/to your models.

### Extensive type support

Dates, enums, byte arrays, etc.

### Optimized performance

AFAIK, fastest SQLite ORM for Android, often on a par with native SQL.

### View support

Create data models from views instead of raw tables.

### Immutable support

You can use final fields in your data objects, with some caveats.

1. All final fields need to be included in a single constructor, with the same type and name as the field. You can have other constructors, and you can make the constructor protected or package-local (not private), but it needs to be there.
2. Generated id values cannot be final, which should be fairly obvious.
3. Foreign fields can be final, but their fields cannot (unless its an eager fetch, but that's still TODO).
4. Foreign collections are out (but really?)

### SQLCipher support

SQLCipher is an encrypted version of SQLite. AFAIK, no off-the-shelf ORM supports this currently, except Realm.  Squeaky will let you do this.  Support is very new, so pardon the dust.

### Foreign Map

To facilitate "proper" thread boundary observation, IE disk I/O in a background thread, you can supply a map to explicitly load foreign references rather than relying on lazy loading or
 annotation based config.

## Similarities to ORMLite

### DatabaseTable

You annotate classes with @DatabaseTable.  Its basically the same.

### DatabaseField

This is mostly the same, although there are some differences.  defaultValue is only useful for schema generation.  If you set a null, it'll stay null.  Some other features didn't make sense on Android.  Also, the 'version' concept is gone.  May be added later if there's sufficient demand, but didn't seem all that useful.

### ID

ID's can be integral or string.  generatedId is there.  Squences are gone.

### TableUtils

Some of the method signitures are modified, but they do the same thing.  Make tables.

### Dao

The main access to data objects is through the Dao interface.  However, custom Dao implementations would be a nightmare.  The interface mostly exists for compatibility, and (possibly) for testing.

## Differences

### DatabaseView

You can query views with @DatabaseView.  The syntax of the fields is basically the same at with tables.  Currenty you can't update views, although SQLite would allow you to do this with triggers.  To discuss.

### Queries

Because we're generating source to read/write to and from the db, the field list in a query needs to be fixed.  Also, there's no "group by" and similar.  As such, a general select statement isn't useful.  You can create a Where object to programatically build a query, or supply the where clause directly with a string.

### Object cache

There's no long-lived object cache.  This may be something for the future, but in general it feels like a recipe for disaster.  There *may* be one for recursive eager fetching, but its only really useful in foreign collections.

### ForeignCollection

Foreign collections are kind of nasty.  They're useful to simplify development, but you can easily get into trouble with them.  Also, if you have lazy collections, you could be diving into the DB in the main thread without realizing it, so lazy loading is manual.  However, since most data models in apps are extremely small, it would be painful not to include a foreign collection.  Just be careful out there.

