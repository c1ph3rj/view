package tech.c1ph3rj.view;


import static tech.c1ph3rj.view.Services.base_version;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TokenTable = "tokenvalue";
    private static final String TokenTableTPA = "tokenValueTPA";
    private static final String refreshTokenHRM = "refreshTokenHRM";
    private static final String refreshTokenTPA = "refreshTokenTPA";
    private static final String offlineTableTPA = "offlineTableTPA";
    private static final String offlineTableVideoTPA = "offlineTableVideoTPA";
    private static final String fireBaseSubscriptionTPA = "fireBaseSubscriptionTPA";

    private static final String baseUrl = "baseUrl";

    public DatabaseHelper(@Nullable Context context) {
        super(context, "StarExecutiveApp", null, base_version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE " + TokenTable + "(tokenValue TEXT)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TokenTable);
            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean insertToken(String token) {
        try {

            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("TokenValue", AESCrypt.encrypt(token));
            long result = db.insert(TokenTable, null, contentValues);
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        } catch (Exception ex) {
            ex.getStackTrace();
            return false;
        }

    }

    public Cursor getTokenDetails() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("Select * from " + TokenTable, null);
        return res;
    }

    public void deleteTokenData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TokenTable);
        return;
    }
}

