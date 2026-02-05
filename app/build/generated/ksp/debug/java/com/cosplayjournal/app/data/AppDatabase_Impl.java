package com.cosplayjournal.app.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.cosplayjournal.app.data.dao.CosplayDao;
import com.cosplayjournal.app.data.dao.CosplayDao_Impl;
import com.cosplayjournal.app.data.dao.LocationDao;
import com.cosplayjournal.app.data.dao.LocationDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile CosplayDao _cosplayDao;

  private volatile LocationDao _locationDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(5) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `cosplans` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `status` TEXT NOT NULL, `tags` TEXT NOT NULL, `season` TEXT NOT NULL, `difficulty` TEXT NOT NULL, `estimatedBudget` REAL NOT NULL, `realBudget` REAL NOT NULL, `notes` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cosplays` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cosplanId` INTEGER, `characterName` TEXT NOT NULL, `series` TEXT NOT NULL, `preferredWeather` TEXT NOT NULL, `wigs` TEXT NOT NULL, `makeup` TEXT NOT NULL, `accessories` TEXT NOT NULL, `notes` TEXT NOT NULL, `recognition` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `isFavorite` INTEGER NOT NULL, `mainImageUri` TEXT, FOREIGN KEY(`cosplanId`) REFERENCES `cosplans`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cosplays_cosplanId` ON `cosplays` (`cosplanId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `handmade_parts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cosplayId` INTEGER NOT NULL, `name` TEXT NOT NULL, `imageUris` TEXT NOT NULL, `price` REAL NOT NULL, `timeSpent` TEXT NOT NULL, `processDescription` TEXT NOT NULL, `projectPercentage` INTEGER NOT NULL, `materials` TEXT NOT NULL, `isFinished` INTEGER NOT NULL, FOREIGN KEY(`cosplayId`) REFERENCES `cosplays`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_handmade_parts_cosplayId` ON `handmade_parts` (`cosplayId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `part_resources` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `partId` INTEGER NOT NULL, `name` TEXT NOT NULL, `webLink` TEXT NOT NULL, `price` REAL NOT NULL, `imageUris` TEXT NOT NULL, `usageDescription` TEXT NOT NULL, FOREIGN KEY(`partId`) REFERENCES `handmade_parts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_part_resources_partId` ON `part_resources` (`partId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `purchased_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cosplayId` INTEGER NOT NULL, `name` TEXT NOT NULL, `purchaseLink` TEXT NOT NULL, `imageUris` TEXT NOT NULL, `adjustmentDescription` TEXT NOT NULL, `projectPercentage` INTEGER NOT NULL, `storeName` TEXT NOT NULL, `price` REAL NOT NULL, `isReceived` INTEGER NOT NULL, FOREIGN KEY(`cosplayId`) REFERENCES `cosplays`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_purchased_items_cosplayId` ON `purchased_items` (`cosplayId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `character_references` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `characterName` TEXT NOT NULL, `series` TEXT NOT NULL, `imageUri` TEXT NOT NULL, `notes` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `locations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `notes` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cosplay_reference_cross_ref` (`cosplayId` INTEGER NOT NULL, `referenceId` INTEGER NOT NULL, PRIMARY KEY(`cosplayId`, `referenceId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cosplay_reference_cross_ref_referenceId` ON `cosplay_reference_cross_ref` (`referenceId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `photo_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` INTEGER NOT NULL, `photographer` TEXT NOT NULL, `notes` TEXT NOT NULL, `locationId` INTEGER, FOREIGN KEY(`locationId`) REFERENCES `locations`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_photo_sessions_locationId` ON `photo_sessions` (`locationId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cosplay_photosession_cross_ref` (`cosplayId` INTEGER NOT NULL, `photoSessionId` INTEGER NOT NULL, PRIMARY KEY(`cosplayId`, `photoSessionId`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cosplay_photosession_cross_ref_photoSessionId` ON `cosplay_photosession_cross_ref` (`photoSessionId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_event_data` (`eventId` TEXT NOT NULL, `status` TEXT NOT NULL, `isFavorite` INTEGER NOT NULL, PRIMARY KEY(`eventId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `event_cosplan_selection` (`eventId` TEXT NOT NULL, `cosplanId` INTEGER NOT NULL, `day` TEXT NOT NULL, PRIMARY KEY(`eventId`, `cosplanId`, `day`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '71e6ee0e65af7506ac176253fa3d2f01')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `cosplans`");
        db.execSQL("DROP TABLE IF EXISTS `cosplays`");
        db.execSQL("DROP TABLE IF EXISTS `handmade_parts`");
        db.execSQL("DROP TABLE IF EXISTS `part_resources`");
        db.execSQL("DROP TABLE IF EXISTS `purchased_items`");
        db.execSQL("DROP TABLE IF EXISTS `character_references`");
        db.execSQL("DROP TABLE IF EXISTS `locations`");
        db.execSQL("DROP TABLE IF EXISTS `cosplay_reference_cross_ref`");
        db.execSQL("DROP TABLE IF EXISTS `photo_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `cosplay_photosession_cross_ref`");
        db.execSQL("DROP TABLE IF EXISTS `user_event_data`");
        db.execSQL("DROP TABLE IF EXISTS `event_cosplan_selection`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCosplans = new HashMap<String, TableInfo.Column>(10);
        _columnsCosplans.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplans.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplans.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplans.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplans.put("tags", new TableInfo.Column("tags", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplans.put("season", new TableInfo.Column("season", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplans.put("difficulty", new TableInfo.Column("difficulty", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplans.put("estimatedBudget", new TableInfo.Column("estimatedBudget", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplans.put("realBudget", new TableInfo.Column("realBudget", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplans.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCosplans = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCosplans = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCosplans = new TableInfo("cosplans", _columnsCosplans, _foreignKeysCosplans, _indicesCosplans);
        final TableInfo _existingCosplans = TableInfo.read(db, "cosplans");
        if (!_infoCosplans.equals(_existingCosplans)) {
          return new RoomOpenHelper.ValidationResult(false, "cosplans(com.cosplayjournal.app.data.entity.Cosplan).\n"
                  + " Expected:\n" + _infoCosplans + "\n"
                  + " Found:\n" + _existingCosplans);
        }
        final HashMap<String, TableInfo.Column> _columnsCosplays = new HashMap<String, TableInfo.Column>(13);
        _columnsCosplays.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplays.put("cosplanId", new TableInfo.Column("cosplanId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplays.put("characterName", new TableInfo.Column("characterName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplays.put("series", new TableInfo.Column("series", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplays.put("preferredWeather", new TableInfo.Column("preferredWeather", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplays.put("wigs", new TableInfo.Column("wigs", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplays.put("makeup", new TableInfo.Column("makeup", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplays.put("accessories", new TableInfo.Column("accessories", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplays.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplays.put("recognition", new TableInfo.Column("recognition", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplays.put("isCompleted", new TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplays.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplays.put("mainImageUri", new TableInfo.Column("mainImageUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCosplays = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysCosplays.add(new TableInfo.ForeignKey("cosplans", "SET NULL", "NO ACTION", Arrays.asList("cosplanId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesCosplays = new HashSet<TableInfo.Index>(1);
        _indicesCosplays.add(new TableInfo.Index("index_cosplays_cosplanId", false, Arrays.asList("cosplanId"), Arrays.asList("ASC")));
        final TableInfo _infoCosplays = new TableInfo("cosplays", _columnsCosplays, _foreignKeysCosplays, _indicesCosplays);
        final TableInfo _existingCosplays = TableInfo.read(db, "cosplays");
        if (!_infoCosplays.equals(_existingCosplays)) {
          return new RoomOpenHelper.ValidationResult(false, "cosplays(com.cosplayjournal.app.data.entity.Cosplay).\n"
                  + " Expected:\n" + _infoCosplays + "\n"
                  + " Found:\n" + _existingCosplays);
        }
        final HashMap<String, TableInfo.Column> _columnsHandmadeParts = new HashMap<String, TableInfo.Column>(10);
        _columnsHandmadeParts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHandmadeParts.put("cosplayId", new TableInfo.Column("cosplayId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHandmadeParts.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHandmadeParts.put("imageUris", new TableInfo.Column("imageUris", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHandmadeParts.put("price", new TableInfo.Column("price", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHandmadeParts.put("timeSpent", new TableInfo.Column("timeSpent", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHandmadeParts.put("processDescription", new TableInfo.Column("processDescription", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHandmadeParts.put("projectPercentage", new TableInfo.Column("projectPercentage", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHandmadeParts.put("materials", new TableInfo.Column("materials", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHandmadeParts.put("isFinished", new TableInfo.Column("isFinished", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHandmadeParts = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysHandmadeParts.add(new TableInfo.ForeignKey("cosplays", "CASCADE", "NO ACTION", Arrays.asList("cosplayId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesHandmadeParts = new HashSet<TableInfo.Index>(1);
        _indicesHandmadeParts.add(new TableInfo.Index("index_handmade_parts_cosplayId", false, Arrays.asList("cosplayId"), Arrays.asList("ASC")));
        final TableInfo _infoHandmadeParts = new TableInfo("handmade_parts", _columnsHandmadeParts, _foreignKeysHandmadeParts, _indicesHandmadeParts);
        final TableInfo _existingHandmadeParts = TableInfo.read(db, "handmade_parts");
        if (!_infoHandmadeParts.equals(_existingHandmadeParts)) {
          return new RoomOpenHelper.ValidationResult(false, "handmade_parts(com.cosplayjournal.app.data.entity.HandmadePart).\n"
                  + " Expected:\n" + _infoHandmadeParts + "\n"
                  + " Found:\n" + _existingHandmadeParts);
        }
        final HashMap<String, TableInfo.Column> _columnsPartResources = new HashMap<String, TableInfo.Column>(7);
        _columnsPartResources.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPartResources.put("partId", new TableInfo.Column("partId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPartResources.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPartResources.put("webLink", new TableInfo.Column("webLink", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPartResources.put("price", new TableInfo.Column("price", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPartResources.put("imageUris", new TableInfo.Column("imageUris", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPartResources.put("usageDescription", new TableInfo.Column("usageDescription", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPartResources = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysPartResources.add(new TableInfo.ForeignKey("handmade_parts", "CASCADE", "NO ACTION", Arrays.asList("partId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesPartResources = new HashSet<TableInfo.Index>(1);
        _indicesPartResources.add(new TableInfo.Index("index_part_resources_partId", false, Arrays.asList("partId"), Arrays.asList("ASC")));
        final TableInfo _infoPartResources = new TableInfo("part_resources", _columnsPartResources, _foreignKeysPartResources, _indicesPartResources);
        final TableInfo _existingPartResources = TableInfo.read(db, "part_resources");
        if (!_infoPartResources.equals(_existingPartResources)) {
          return new RoomOpenHelper.ValidationResult(false, "part_resources(com.cosplayjournal.app.data.entity.PartResource).\n"
                  + " Expected:\n" + _infoPartResources + "\n"
                  + " Found:\n" + _existingPartResources);
        }
        final HashMap<String, TableInfo.Column> _columnsPurchasedItems = new HashMap<String, TableInfo.Column>(10);
        _columnsPurchasedItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchasedItems.put("cosplayId", new TableInfo.Column("cosplayId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchasedItems.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchasedItems.put("purchaseLink", new TableInfo.Column("purchaseLink", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchasedItems.put("imageUris", new TableInfo.Column("imageUris", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchasedItems.put("adjustmentDescription", new TableInfo.Column("adjustmentDescription", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchasedItems.put("projectPercentage", new TableInfo.Column("projectPercentage", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchasedItems.put("storeName", new TableInfo.Column("storeName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchasedItems.put("price", new TableInfo.Column("price", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPurchasedItems.put("isReceived", new TableInfo.Column("isReceived", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPurchasedItems = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysPurchasedItems.add(new TableInfo.ForeignKey("cosplays", "CASCADE", "NO ACTION", Arrays.asList("cosplayId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesPurchasedItems = new HashSet<TableInfo.Index>(1);
        _indicesPurchasedItems.add(new TableInfo.Index("index_purchased_items_cosplayId", false, Arrays.asList("cosplayId"), Arrays.asList("ASC")));
        final TableInfo _infoPurchasedItems = new TableInfo("purchased_items", _columnsPurchasedItems, _foreignKeysPurchasedItems, _indicesPurchasedItems);
        final TableInfo _existingPurchasedItems = TableInfo.read(db, "purchased_items");
        if (!_infoPurchasedItems.equals(_existingPurchasedItems)) {
          return new RoomOpenHelper.ValidationResult(false, "purchased_items(com.cosplayjournal.app.data.entity.PurchasedItem).\n"
                  + " Expected:\n" + _infoPurchasedItems + "\n"
                  + " Found:\n" + _existingPurchasedItems);
        }
        final HashMap<String, TableInfo.Column> _columnsCharacterReferences = new HashMap<String, TableInfo.Column>(5);
        _columnsCharacterReferences.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCharacterReferences.put("characterName", new TableInfo.Column("characterName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCharacterReferences.put("series", new TableInfo.Column("series", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCharacterReferences.put("imageUri", new TableInfo.Column("imageUri", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCharacterReferences.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCharacterReferences = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCharacterReferences = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCharacterReferences = new TableInfo("character_references", _columnsCharacterReferences, _foreignKeysCharacterReferences, _indicesCharacterReferences);
        final TableInfo _existingCharacterReferences = TableInfo.read(db, "character_references");
        if (!_infoCharacterReferences.equals(_existingCharacterReferences)) {
          return new RoomOpenHelper.ValidationResult(false, "character_references(com.cosplayjournal.app.data.entity.CharacterReference).\n"
                  + " Expected:\n" + _infoCharacterReferences + "\n"
                  + " Found:\n" + _existingCharacterReferences);
        }
        final HashMap<String, TableInfo.Column> _columnsLocations = new HashMap<String, TableInfo.Column>(5);
        _columnsLocations.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocations.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocations.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocations.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocations.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLocations = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLocations = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLocations = new TableInfo("locations", _columnsLocations, _foreignKeysLocations, _indicesLocations);
        final TableInfo _existingLocations = TableInfo.read(db, "locations");
        if (!_infoLocations.equals(_existingLocations)) {
          return new RoomOpenHelper.ValidationResult(false, "locations(com.cosplayjournal.app.data.entity.Location).\n"
                  + " Expected:\n" + _infoLocations + "\n"
                  + " Found:\n" + _existingLocations);
        }
        final HashMap<String, TableInfo.Column> _columnsCosplayReferenceCrossRef = new HashMap<String, TableInfo.Column>(2);
        _columnsCosplayReferenceCrossRef.put("cosplayId", new TableInfo.Column("cosplayId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplayReferenceCrossRef.put("referenceId", new TableInfo.Column("referenceId", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCosplayReferenceCrossRef = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCosplayReferenceCrossRef = new HashSet<TableInfo.Index>(1);
        _indicesCosplayReferenceCrossRef.add(new TableInfo.Index("index_cosplay_reference_cross_ref_referenceId", false, Arrays.asList("referenceId"), Arrays.asList("ASC")));
        final TableInfo _infoCosplayReferenceCrossRef = new TableInfo("cosplay_reference_cross_ref", _columnsCosplayReferenceCrossRef, _foreignKeysCosplayReferenceCrossRef, _indicesCosplayReferenceCrossRef);
        final TableInfo _existingCosplayReferenceCrossRef = TableInfo.read(db, "cosplay_reference_cross_ref");
        if (!_infoCosplayReferenceCrossRef.equals(_existingCosplayReferenceCrossRef)) {
          return new RoomOpenHelper.ValidationResult(false, "cosplay_reference_cross_ref(com.cosplayjournal.app.data.entity.CosplayReferenceCrossRef).\n"
                  + " Expected:\n" + _infoCosplayReferenceCrossRef + "\n"
                  + " Found:\n" + _existingCosplayReferenceCrossRef);
        }
        final HashMap<String, TableInfo.Column> _columnsPhotoSessions = new HashMap<String, TableInfo.Column>(5);
        _columnsPhotoSessions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotoSessions.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotoSessions.put("photographer", new TableInfo.Column("photographer", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotoSessions.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPhotoSessions.put("locationId", new TableInfo.Column("locationId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPhotoSessions = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysPhotoSessions.add(new TableInfo.ForeignKey("locations", "SET NULL", "NO ACTION", Arrays.asList("locationId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesPhotoSessions = new HashSet<TableInfo.Index>(1);
        _indicesPhotoSessions.add(new TableInfo.Index("index_photo_sessions_locationId", false, Arrays.asList("locationId"), Arrays.asList("ASC")));
        final TableInfo _infoPhotoSessions = new TableInfo("photo_sessions", _columnsPhotoSessions, _foreignKeysPhotoSessions, _indicesPhotoSessions);
        final TableInfo _existingPhotoSessions = TableInfo.read(db, "photo_sessions");
        if (!_infoPhotoSessions.equals(_existingPhotoSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "photo_sessions(com.cosplayjournal.app.data.entity.PhotoSession).\n"
                  + " Expected:\n" + _infoPhotoSessions + "\n"
                  + " Found:\n" + _existingPhotoSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsCosplayPhotosessionCrossRef = new HashMap<String, TableInfo.Column>(2);
        _columnsCosplayPhotosessionCrossRef.put("cosplayId", new TableInfo.Column("cosplayId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCosplayPhotosessionCrossRef.put("photoSessionId", new TableInfo.Column("photoSessionId", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCosplayPhotosessionCrossRef = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCosplayPhotosessionCrossRef = new HashSet<TableInfo.Index>(1);
        _indicesCosplayPhotosessionCrossRef.add(new TableInfo.Index("index_cosplay_photosession_cross_ref_photoSessionId", false, Arrays.asList("photoSessionId"), Arrays.asList("ASC")));
        final TableInfo _infoCosplayPhotosessionCrossRef = new TableInfo("cosplay_photosession_cross_ref", _columnsCosplayPhotosessionCrossRef, _foreignKeysCosplayPhotosessionCrossRef, _indicesCosplayPhotosessionCrossRef);
        final TableInfo _existingCosplayPhotosessionCrossRef = TableInfo.read(db, "cosplay_photosession_cross_ref");
        if (!_infoCosplayPhotosessionCrossRef.equals(_existingCosplayPhotosessionCrossRef)) {
          return new RoomOpenHelper.ValidationResult(false, "cosplay_photosession_cross_ref(com.cosplayjournal.app.data.entity.CosplayPhotoSessionCrossRef).\n"
                  + " Expected:\n" + _infoCosplayPhotosessionCrossRef + "\n"
                  + " Found:\n" + _existingCosplayPhotosessionCrossRef);
        }
        final HashMap<String, TableInfo.Column> _columnsUserEventData = new HashMap<String, TableInfo.Column>(3);
        _columnsUserEventData.put("eventId", new TableInfo.Column("eventId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserEventData.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserEventData.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserEventData = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUserEventData = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserEventData = new TableInfo("user_event_data", _columnsUserEventData, _foreignKeysUserEventData, _indicesUserEventData);
        final TableInfo _existingUserEventData = TableInfo.read(db, "user_event_data");
        if (!_infoUserEventData.equals(_existingUserEventData)) {
          return new RoomOpenHelper.ValidationResult(false, "user_event_data(com.cosplayjournal.app.data.entity.UserEventData).\n"
                  + " Expected:\n" + _infoUserEventData + "\n"
                  + " Found:\n" + _existingUserEventData);
        }
        final HashMap<String, TableInfo.Column> _columnsEventCosplanSelection = new HashMap<String, TableInfo.Column>(3);
        _columnsEventCosplanSelection.put("eventId", new TableInfo.Column("eventId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEventCosplanSelection.put("cosplanId", new TableInfo.Column("cosplanId", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEventCosplanSelection.put("day", new TableInfo.Column("day", "TEXT", true, 3, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEventCosplanSelection = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEventCosplanSelection = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEventCosplanSelection = new TableInfo("event_cosplan_selection", _columnsEventCosplanSelection, _foreignKeysEventCosplanSelection, _indicesEventCosplanSelection);
        final TableInfo _existingEventCosplanSelection = TableInfo.read(db, "event_cosplan_selection");
        if (!_infoEventCosplanSelection.equals(_existingEventCosplanSelection)) {
          return new RoomOpenHelper.ValidationResult(false, "event_cosplan_selection(com.cosplayjournal.app.data.entity.EventCosplanSelection).\n"
                  + " Expected:\n" + _infoEventCosplanSelection + "\n"
                  + " Found:\n" + _existingEventCosplanSelection);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "71e6ee0e65af7506ac176253fa3d2f01", "d1e5369b6842bf321959ef440019f5c1");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "cosplans","cosplays","handmade_parts","part_resources","purchased_items","character_references","locations","cosplay_reference_cross_ref","photo_sessions","cosplay_photosession_cross_ref","user_event_data","event_cosplan_selection");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `cosplans`");
      _db.execSQL("DELETE FROM `cosplays`");
      _db.execSQL("DELETE FROM `handmade_parts`");
      _db.execSQL("DELETE FROM `part_resources`");
      _db.execSQL("DELETE FROM `purchased_items`");
      _db.execSQL("DELETE FROM `character_references`");
      _db.execSQL("DELETE FROM `locations`");
      _db.execSQL("DELETE FROM `cosplay_reference_cross_ref`");
      _db.execSQL("DELETE FROM `photo_sessions`");
      _db.execSQL("DELETE FROM `cosplay_photosession_cross_ref`");
      _db.execSQL("DELETE FROM `user_event_data`");
      _db.execSQL("DELETE FROM `event_cosplan_selection`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CosplayDao.class, CosplayDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(LocationDao.class, LocationDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CosplayDao cosplayDao() {
    if (_cosplayDao != null) {
      return _cosplayDao;
    } else {
      synchronized(this) {
        if(_cosplayDao == null) {
          _cosplayDao = new CosplayDao_Impl(this);
        }
        return _cosplayDao;
      }
    }
  }

  @Override
  public LocationDao locationDao() {
    if (_locationDao != null) {
      return _locationDao;
    } else {
      synchronized(this) {
        if(_locationDao == null) {
          _locationDao = new LocationDao_Impl(this);
        }
        return _locationDao;
      }
    }
  }
}
