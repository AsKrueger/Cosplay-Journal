package com.cosplayjournal.app.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.cosplayjournal.app.data.entity.Cosplan;
import com.cosplayjournal.app.data.entity.Cosplay;
import com.cosplayjournal.app.data.entity.EventCosplanSelection;
import com.cosplayjournal.app.data.entity.HandmadePart;
import com.cosplayjournal.app.data.entity.PurchasedItem;
import com.cosplayjournal.app.data.entity.UserEventData;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CosplayDao_Impl implements CosplayDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Cosplan> __insertionAdapterOfCosplan;

  private final EntityInsertionAdapter<Cosplay> __insertionAdapterOfCosplay;

  private final EntityInsertionAdapter<HandmadePart> __insertionAdapterOfHandmadePart;

  private final EntityInsertionAdapter<PurchasedItem> __insertionAdapterOfPurchasedItem;

  private final EntityInsertionAdapter<UserEventData> __insertionAdapterOfUserEventData;

  private final EntityInsertionAdapter<EventCosplanSelection> __insertionAdapterOfEventCosplanSelection;

  private final EntityDeletionOrUpdateAdapter<Cosplan> __deletionAdapterOfCosplan;

  private final EntityDeletionOrUpdateAdapter<Cosplay> __deletionAdapterOfCosplay;

  private final EntityDeletionOrUpdateAdapter<HandmadePart> __deletionAdapterOfHandmadePart;

  private final EntityDeletionOrUpdateAdapter<PurchasedItem> __deletionAdapterOfPurchasedItem;

  private final EntityDeletionOrUpdateAdapter<Cosplan> __updateAdapterOfCosplan;

  private final EntityDeletionOrUpdateAdapter<Cosplay> __updateAdapterOfCosplay;

  private final EntityDeletionOrUpdateAdapter<HandmadePart> __updateAdapterOfHandmadePart;

  private final EntityDeletionOrUpdateAdapter<PurchasedItem> __updateAdapterOfPurchasedItem;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCosplanSelection;

  public CosplayDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCosplan = new EntityInsertionAdapter<Cosplan>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cosplans` (`id`,`name`,`description`,`status`,`tags`,`season`,`difficulty`,`estimatedBudget`,`realBudget`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Cosplan entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDescription());
        statement.bindString(4, entity.getStatus());
        statement.bindString(5, entity.getTags());
        statement.bindString(6, entity.getSeason());
        statement.bindString(7, entity.getDifficulty());
        statement.bindDouble(8, entity.getEstimatedBudget());
        statement.bindDouble(9, entity.getRealBudget());
        statement.bindString(10, entity.getNotes());
      }
    };
    this.__insertionAdapterOfCosplay = new EntityInsertionAdapter<Cosplay>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `cosplays` (`id`,`cosplanId`,`characterName`,`series`,`wigs`,`makeup`,`accessories`,`notes`,`isFavorite`,`mainImageUri`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Cosplay entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getCosplanId());
        statement.bindString(3, entity.getCharacterName());
        statement.bindString(4, entity.getSeries());
        statement.bindString(5, entity.getWigs());
        statement.bindString(6, entity.getMakeup());
        statement.bindString(7, entity.getAccessories());
        statement.bindString(8, entity.getNotes());
        final int _tmp = entity.isFavorite() ? 1 : 0;
        statement.bindLong(9, _tmp);
        if (entity.getMainImageUri() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getMainImageUri());
        }
      }
    };
    this.__insertionAdapterOfHandmadePart = new EntityInsertionAdapter<HandmadePart>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `handmade_parts` (`id`,`cosplayId`,`name`,`processSteps`,`materials`,`estimatedCost`,`isFinished`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HandmadePart entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getCosplayId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getProcessSteps());
        statement.bindString(5, entity.getMaterials());
        statement.bindDouble(6, entity.getEstimatedCost());
        final int _tmp = entity.isFinished() ? 1 : 0;
        statement.bindLong(7, _tmp);
      }
    };
    this.__insertionAdapterOfPurchasedItem = new EntityInsertionAdapter<PurchasedItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `purchased_items` (`id`,`cosplayId`,`name`,`storeName`,`purchaseLink`,`price`,`isReceived`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PurchasedItem entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getCosplayId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getStoreName());
        statement.bindString(5, entity.getPurchaseLink());
        statement.bindDouble(6, entity.getPrice());
        final int _tmp = entity.isReceived() ? 1 : 0;
        statement.bindLong(7, _tmp);
      }
    };
    this.__insertionAdapterOfUserEventData = new EntityInsertionAdapter<UserEventData>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `user_event_data` (`eventId`,`status`,`isFavorite`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserEventData entity) {
        statement.bindString(1, entity.getEventId());
        statement.bindString(2, entity.getStatus());
        final int _tmp = entity.isFavorite() ? 1 : 0;
        statement.bindLong(3, _tmp);
      }
    };
    this.__insertionAdapterOfEventCosplanSelection = new EntityInsertionAdapter<EventCosplanSelection>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `event_cosplan_selection` (`eventId`,`cosplanId`,`day`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EventCosplanSelection entity) {
        statement.bindString(1, entity.getEventId());
        statement.bindLong(2, entity.getCosplanId());
        statement.bindString(3, entity.getDay());
      }
    };
    this.__deletionAdapterOfCosplan = new EntityDeletionOrUpdateAdapter<Cosplan>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `cosplans` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Cosplan entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__deletionAdapterOfCosplay = new EntityDeletionOrUpdateAdapter<Cosplay>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `cosplays` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Cosplay entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__deletionAdapterOfHandmadePart = new EntityDeletionOrUpdateAdapter<HandmadePart>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `handmade_parts` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HandmadePart entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__deletionAdapterOfPurchasedItem = new EntityDeletionOrUpdateAdapter<PurchasedItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `purchased_items` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PurchasedItem entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfCosplan = new EntityDeletionOrUpdateAdapter<Cosplan>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `cosplans` SET `id` = ?,`name` = ?,`description` = ?,`status` = ?,`tags` = ?,`season` = ?,`difficulty` = ?,`estimatedBudget` = ?,`realBudget` = ?,`notes` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Cosplan entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDescription());
        statement.bindString(4, entity.getStatus());
        statement.bindString(5, entity.getTags());
        statement.bindString(6, entity.getSeason());
        statement.bindString(7, entity.getDifficulty());
        statement.bindDouble(8, entity.getEstimatedBudget());
        statement.bindDouble(9, entity.getRealBudget());
        statement.bindString(10, entity.getNotes());
        statement.bindLong(11, entity.getId());
      }
    };
    this.__updateAdapterOfCosplay = new EntityDeletionOrUpdateAdapter<Cosplay>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `cosplays` SET `id` = ?,`cosplanId` = ?,`characterName` = ?,`series` = ?,`wigs` = ?,`makeup` = ?,`accessories` = ?,`notes` = ?,`isFavorite` = ?,`mainImageUri` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Cosplay entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getCosplanId());
        statement.bindString(3, entity.getCharacterName());
        statement.bindString(4, entity.getSeries());
        statement.bindString(5, entity.getWigs());
        statement.bindString(6, entity.getMakeup());
        statement.bindString(7, entity.getAccessories());
        statement.bindString(8, entity.getNotes());
        final int _tmp = entity.isFavorite() ? 1 : 0;
        statement.bindLong(9, _tmp);
        if (entity.getMainImageUri() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getMainImageUri());
        }
        statement.bindLong(11, entity.getId());
      }
    };
    this.__updateAdapterOfHandmadePart = new EntityDeletionOrUpdateAdapter<HandmadePart>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `handmade_parts` SET `id` = ?,`cosplayId` = ?,`name` = ?,`processSteps` = ?,`materials` = ?,`estimatedCost` = ?,`isFinished` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HandmadePart entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getCosplayId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getProcessSteps());
        statement.bindString(5, entity.getMaterials());
        statement.bindDouble(6, entity.getEstimatedCost());
        final int _tmp = entity.isFinished() ? 1 : 0;
        statement.bindLong(7, _tmp);
        statement.bindLong(8, entity.getId());
      }
    };
    this.__updateAdapterOfPurchasedItem = new EntityDeletionOrUpdateAdapter<PurchasedItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `purchased_items` SET `id` = ?,`cosplayId` = ?,`name` = ?,`storeName` = ?,`purchaseLink` = ?,`price` = ?,`isReceived` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PurchasedItem entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getCosplayId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getStoreName());
        statement.bindString(5, entity.getPurchaseLink());
        statement.bindDouble(6, entity.getPrice());
        final int _tmp = entity.isReceived() ? 1 : 0;
        statement.bindLong(7, _tmp);
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteCosplanSelection = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM event_cosplan_selection WHERE eventId = ? AND cosplanId = ? AND day = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertCosplan(final Cosplan cosplan, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCosplan.insertAndReturnId(cosplan);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertCosplay(final Cosplay cosplay, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCosplay.insertAndReturnId(cosplay);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertHandmadePart(final HandmadePart part,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfHandmadePart.insert(part);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertPurchasedItem(final PurchasedItem item,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPurchasedItem.insert(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertUserEventData(final UserEventData data,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserEventData.insert(data);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertCosplanSelection(final EventCosplanSelection selection,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEventCosplanSelection.insert(selection);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCosplan(final Cosplan cosplan, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfCosplan.handle(cosplan);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCosplay(final Cosplay cosplay, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfCosplay.handle(cosplay);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteHandmadePart(final HandmadePart part,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfHandmadePart.handle(part);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePurchasedItem(final PurchasedItem item,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPurchasedItem.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCosplan(final Cosplan cosplan, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCosplan.handle(cosplan);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCosplay(final Cosplay cosplay, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCosplay.handle(cosplay);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateHandmadePart(final HandmadePart part,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfHandmadePart.handle(part);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePurchasedItem(final PurchasedItem item,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPurchasedItem.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCosplanSelection(final String eventId, final long cosplanId, final String day,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCosplanSelection.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, eventId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, cosplanId);
        _argIndex = 3;
        _stmt.bindString(_argIndex, day);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCosplanSelection.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Cosplan>> getAllCosplans() {
    final String _sql = "SELECT * FROM cosplans";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cosplans"}, new Callable<List<Cosplan>>() {
      @Override
      @NonNull
      public List<Cosplan> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfSeason = CursorUtil.getColumnIndexOrThrow(_cursor, "season");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfEstimatedBudget = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedBudget");
          final int _cursorIndexOfRealBudget = CursorUtil.getColumnIndexOrThrow(_cursor, "realBudget");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<Cosplan> _result = new ArrayList<Cosplan>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Cosplan _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final String _tmpSeason;
            _tmpSeason = _cursor.getString(_cursorIndexOfSeason);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final double _tmpEstimatedBudget;
            _tmpEstimatedBudget = _cursor.getDouble(_cursorIndexOfEstimatedBudget);
            final double _tmpRealBudget;
            _tmpRealBudget = _cursor.getDouble(_cursorIndexOfRealBudget);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            _item = new Cosplan(_tmpId,_tmpName,_tmpDescription,_tmpStatus,_tmpTags,_tmpSeason,_tmpDifficulty,_tmpEstimatedBudget,_tmpRealBudget,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Cosplay>> getAllCosplays() {
    final String _sql = "SELECT * FROM cosplays";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cosplays"}, new Callable<List<Cosplay>>() {
      @Override
      @NonNull
      public List<Cosplay> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCosplanId = CursorUtil.getColumnIndexOrThrow(_cursor, "cosplanId");
          final int _cursorIndexOfCharacterName = CursorUtil.getColumnIndexOrThrow(_cursor, "characterName");
          final int _cursorIndexOfSeries = CursorUtil.getColumnIndexOrThrow(_cursor, "series");
          final int _cursorIndexOfWigs = CursorUtil.getColumnIndexOrThrow(_cursor, "wigs");
          final int _cursorIndexOfMakeup = CursorUtil.getColumnIndexOrThrow(_cursor, "makeup");
          final int _cursorIndexOfAccessories = CursorUtil.getColumnIndexOrThrow(_cursor, "accessories");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfMainImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "mainImageUri");
          final List<Cosplay> _result = new ArrayList<Cosplay>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Cosplay _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpCosplanId;
            _tmpCosplanId = _cursor.getLong(_cursorIndexOfCosplanId);
            final String _tmpCharacterName;
            _tmpCharacterName = _cursor.getString(_cursorIndexOfCharacterName);
            final String _tmpSeries;
            _tmpSeries = _cursor.getString(_cursorIndexOfSeries);
            final String _tmpWigs;
            _tmpWigs = _cursor.getString(_cursorIndexOfWigs);
            final String _tmpMakeup;
            _tmpMakeup = _cursor.getString(_cursorIndexOfMakeup);
            final String _tmpAccessories;
            _tmpAccessories = _cursor.getString(_cursorIndexOfAccessories);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final String _tmpMainImageUri;
            if (_cursor.isNull(_cursorIndexOfMainImageUri)) {
              _tmpMainImageUri = null;
            } else {
              _tmpMainImageUri = _cursor.getString(_cursorIndexOfMainImageUri);
            }
            _item = new Cosplay(_tmpId,_tmpCosplanId,_tmpCharacterName,_tmpSeries,_tmpWigs,_tmpMakeup,_tmpAccessories,_tmpNotes,_tmpIsFavorite,_tmpMainImageUri);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Cosplay>> getCosplaysForCosplan(final long cosplanId) {
    final String _sql = "SELECT * FROM cosplays WHERE cosplanId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, cosplanId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cosplays"}, new Callable<List<Cosplay>>() {
      @Override
      @NonNull
      public List<Cosplay> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCosplanId = CursorUtil.getColumnIndexOrThrow(_cursor, "cosplanId");
          final int _cursorIndexOfCharacterName = CursorUtil.getColumnIndexOrThrow(_cursor, "characterName");
          final int _cursorIndexOfSeries = CursorUtil.getColumnIndexOrThrow(_cursor, "series");
          final int _cursorIndexOfWigs = CursorUtil.getColumnIndexOrThrow(_cursor, "wigs");
          final int _cursorIndexOfMakeup = CursorUtil.getColumnIndexOrThrow(_cursor, "makeup");
          final int _cursorIndexOfAccessories = CursorUtil.getColumnIndexOrThrow(_cursor, "accessories");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfMainImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "mainImageUri");
          final List<Cosplay> _result = new ArrayList<Cosplay>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Cosplay _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpCosplanId;
            _tmpCosplanId = _cursor.getLong(_cursorIndexOfCosplanId);
            final String _tmpCharacterName;
            _tmpCharacterName = _cursor.getString(_cursorIndexOfCharacterName);
            final String _tmpSeries;
            _tmpSeries = _cursor.getString(_cursorIndexOfSeries);
            final String _tmpWigs;
            _tmpWigs = _cursor.getString(_cursorIndexOfWigs);
            final String _tmpMakeup;
            _tmpMakeup = _cursor.getString(_cursorIndexOfMakeup);
            final String _tmpAccessories;
            _tmpAccessories = _cursor.getString(_cursorIndexOfAccessories);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final String _tmpMainImageUri;
            if (_cursor.isNull(_cursorIndexOfMainImageUri)) {
              _tmpMainImageUri = null;
            } else {
              _tmpMainImageUri = _cursor.getString(_cursorIndexOfMainImageUri);
            }
            _item = new Cosplay(_tmpId,_tmpCosplanId,_tmpCharacterName,_tmpSeries,_tmpWigs,_tmpMakeup,_tmpAccessories,_tmpNotes,_tmpIsFavorite,_tmpMainImageUri);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getCosplayById(final long id, final Continuation<? super Cosplay> $completion) {
    final String _sql = "SELECT * FROM cosplays WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Cosplay>() {
      @Override
      @Nullable
      public Cosplay call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCosplanId = CursorUtil.getColumnIndexOrThrow(_cursor, "cosplanId");
          final int _cursorIndexOfCharacterName = CursorUtil.getColumnIndexOrThrow(_cursor, "characterName");
          final int _cursorIndexOfSeries = CursorUtil.getColumnIndexOrThrow(_cursor, "series");
          final int _cursorIndexOfWigs = CursorUtil.getColumnIndexOrThrow(_cursor, "wigs");
          final int _cursorIndexOfMakeup = CursorUtil.getColumnIndexOrThrow(_cursor, "makeup");
          final int _cursorIndexOfAccessories = CursorUtil.getColumnIndexOrThrow(_cursor, "accessories");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfMainImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "mainImageUri");
          final Cosplay _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpCosplanId;
            _tmpCosplanId = _cursor.getLong(_cursorIndexOfCosplanId);
            final String _tmpCharacterName;
            _tmpCharacterName = _cursor.getString(_cursorIndexOfCharacterName);
            final String _tmpSeries;
            _tmpSeries = _cursor.getString(_cursorIndexOfSeries);
            final String _tmpWigs;
            _tmpWigs = _cursor.getString(_cursorIndexOfWigs);
            final String _tmpMakeup;
            _tmpMakeup = _cursor.getString(_cursorIndexOfMakeup);
            final String _tmpAccessories;
            _tmpAccessories = _cursor.getString(_cursorIndexOfAccessories);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final String _tmpMainImageUri;
            if (_cursor.isNull(_cursorIndexOfMainImageUri)) {
              _tmpMainImageUri = null;
            } else {
              _tmpMainImageUri = _cursor.getString(_cursorIndexOfMainImageUri);
            }
            _result = new Cosplay(_tmpId,_tmpCosplanId,_tmpCharacterName,_tmpSeries,_tmpWigs,_tmpMakeup,_tmpAccessories,_tmpNotes,_tmpIsFavorite,_tmpMainImageUri);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Cosplay>> getFavoriteCosplays() {
    final String _sql = "SELECT * FROM cosplays WHERE isFavorite = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"cosplays"}, new Callable<List<Cosplay>>() {
      @Override
      @NonNull
      public List<Cosplay> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCosplanId = CursorUtil.getColumnIndexOrThrow(_cursor, "cosplanId");
          final int _cursorIndexOfCharacterName = CursorUtil.getColumnIndexOrThrow(_cursor, "characterName");
          final int _cursorIndexOfSeries = CursorUtil.getColumnIndexOrThrow(_cursor, "series");
          final int _cursorIndexOfWigs = CursorUtil.getColumnIndexOrThrow(_cursor, "wigs");
          final int _cursorIndexOfMakeup = CursorUtil.getColumnIndexOrThrow(_cursor, "makeup");
          final int _cursorIndexOfAccessories = CursorUtil.getColumnIndexOrThrow(_cursor, "accessories");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfMainImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "mainImageUri");
          final List<Cosplay> _result = new ArrayList<Cosplay>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Cosplay _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpCosplanId;
            _tmpCosplanId = _cursor.getLong(_cursorIndexOfCosplanId);
            final String _tmpCharacterName;
            _tmpCharacterName = _cursor.getString(_cursorIndexOfCharacterName);
            final String _tmpSeries;
            _tmpSeries = _cursor.getString(_cursorIndexOfSeries);
            final String _tmpWigs;
            _tmpWigs = _cursor.getString(_cursorIndexOfWigs);
            final String _tmpMakeup;
            _tmpMakeup = _cursor.getString(_cursorIndexOfMakeup);
            final String _tmpAccessories;
            _tmpAccessories = _cursor.getString(_cursorIndexOfAccessories);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final String _tmpMainImageUri;
            if (_cursor.isNull(_cursorIndexOfMainImageUri)) {
              _tmpMainImageUri = null;
            } else {
              _tmpMainImageUri = _cursor.getString(_cursorIndexOfMainImageUri);
            }
            _item = new Cosplay(_tmpId,_tmpCosplanId,_tmpCharacterName,_tmpSeries,_tmpWigs,_tmpMakeup,_tmpAccessories,_tmpNotes,_tmpIsFavorite,_tmpMainImageUri);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<HandmadePart>> getHandmadeParts(final long cosplayId) {
    final String _sql = "SELECT * FROM handmade_parts WHERE cosplayId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, cosplayId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"handmade_parts"}, new Callable<List<HandmadePart>>() {
      @Override
      @NonNull
      public List<HandmadePart> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCosplayId = CursorUtil.getColumnIndexOrThrow(_cursor, "cosplayId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfProcessSteps = CursorUtil.getColumnIndexOrThrow(_cursor, "processSteps");
          final int _cursorIndexOfMaterials = CursorUtil.getColumnIndexOrThrow(_cursor, "materials");
          final int _cursorIndexOfEstimatedCost = CursorUtil.getColumnIndexOrThrow(_cursor, "estimatedCost");
          final int _cursorIndexOfIsFinished = CursorUtil.getColumnIndexOrThrow(_cursor, "isFinished");
          final List<HandmadePart> _result = new ArrayList<HandmadePart>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HandmadePart _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpCosplayId;
            _tmpCosplayId = _cursor.getLong(_cursorIndexOfCosplayId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpProcessSteps;
            _tmpProcessSteps = _cursor.getString(_cursorIndexOfProcessSteps);
            final String _tmpMaterials;
            _tmpMaterials = _cursor.getString(_cursorIndexOfMaterials);
            final double _tmpEstimatedCost;
            _tmpEstimatedCost = _cursor.getDouble(_cursorIndexOfEstimatedCost);
            final boolean _tmpIsFinished;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFinished);
            _tmpIsFinished = _tmp != 0;
            _item = new HandmadePart(_tmpId,_tmpCosplayId,_tmpName,_tmpProcessSteps,_tmpMaterials,_tmpEstimatedCost,_tmpIsFinished);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<PurchasedItem>> getPurchasedItems(final long cosplayId) {
    final String _sql = "SELECT * FROM purchased_items WHERE cosplayId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, cosplayId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"purchased_items"}, new Callable<List<PurchasedItem>>() {
      @Override
      @NonNull
      public List<PurchasedItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCosplayId = CursorUtil.getColumnIndexOrThrow(_cursor, "cosplayId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfStoreName = CursorUtil.getColumnIndexOrThrow(_cursor, "storeName");
          final int _cursorIndexOfPurchaseLink = CursorUtil.getColumnIndexOrThrow(_cursor, "purchaseLink");
          final int _cursorIndexOfPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "price");
          final int _cursorIndexOfIsReceived = CursorUtil.getColumnIndexOrThrow(_cursor, "isReceived");
          final List<PurchasedItem> _result = new ArrayList<PurchasedItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PurchasedItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpCosplayId;
            _tmpCosplayId = _cursor.getLong(_cursorIndexOfCosplayId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpStoreName;
            _tmpStoreName = _cursor.getString(_cursorIndexOfStoreName);
            final String _tmpPurchaseLink;
            _tmpPurchaseLink = _cursor.getString(_cursorIndexOfPurchaseLink);
            final double _tmpPrice;
            _tmpPrice = _cursor.getDouble(_cursorIndexOfPrice);
            final boolean _tmpIsReceived;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsReceived);
            _tmpIsReceived = _tmp != 0;
            _item = new PurchasedItem(_tmpId,_tmpCosplayId,_tmpName,_tmpStoreName,_tmpPurchaseLink,_tmpPrice,_tmpIsReceived);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getUserEventData(final String eventId,
      final Continuation<? super UserEventData> $completion) {
    final String _sql = "SELECT * FROM user_event_data WHERE eventId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UserEventData>() {
      @Override
      @Nullable
      public UserEventData call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final UserEventData _result;
          if (_cursor.moveToFirst()) {
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            _result = new UserEventData(_tmpEventId,_tmpStatus,_tmpIsFavorite);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<UserEventData>> getAllUserEventData() {
    final String _sql = "SELECT * FROM user_event_data";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_event_data"}, new Callable<List<UserEventData>>() {
      @Override
      @NonNull
      public List<UserEventData> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final List<UserEventData> _result = new ArrayList<UserEventData>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserEventData _item;
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            _item = new UserEventData(_tmpEventId,_tmpStatus,_tmpIsFavorite);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<EventCosplanSelection>> getCosplanSelectionsForEvent(final String eventId) {
    final String _sql = "SELECT * FROM event_cosplan_selection WHERE eventId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, eventId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"event_cosplan_selection"}, new Callable<List<EventCosplanSelection>>() {
      @Override
      @NonNull
      public List<EventCosplanSelection> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "eventId");
          final int _cursorIndexOfCosplanId = CursorUtil.getColumnIndexOrThrow(_cursor, "cosplanId");
          final int _cursorIndexOfDay = CursorUtil.getColumnIndexOrThrow(_cursor, "day");
          final List<EventCosplanSelection> _result = new ArrayList<EventCosplanSelection>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EventCosplanSelection _item;
            final String _tmpEventId;
            _tmpEventId = _cursor.getString(_cursorIndexOfEventId);
            final long _tmpCosplanId;
            _tmpCosplanId = _cursor.getLong(_cursorIndexOfCosplanId);
            final String _tmpDay;
            _tmpDay = _cursor.getString(_cursorIndexOfDay);
            _item = new EventCosplanSelection(_tmpEventId,_tmpCosplanId,_tmpDay);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
