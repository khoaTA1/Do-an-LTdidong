package vn.ltdidong.apphoctienganh.functions;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private Context context;
    private String DATABASE_NAME = "APPHOCTIENGANH";

    // bảng cho chế độ trắc nghiệm trong kỹ năng đọc
    ///  bảng reading pasage
    private String READINGPASSAGE_TABLE_NAME = "readingpassage";
    private String READINGPASSAGE_COLUMN_ID = "id";
    private String READINGPASSAGE_COLUMN_PASSAGE = "pasage";

    ///  bảng question answer
    private String QA_TABLE_NAME = "questionanswer";
    private String QA_COLUMN_ID = "id";
    private String QA_COLUMN_QUESTION = "pasage";
    private String QA_COLUMN_CORRECTANSWER = "correctanswer";

    ///  bảng answers
    private String ANSWER_TABLE_NAME = "answers";
    private String ANSWER_COLUMN_ID = "id";
    private String ANSWER_COLUMN_DEDICATEDID = "dedicated_id";
    private String ANSWER_DETAIL = "detail";

    /// bảng liên kết bảng reading passage và question answer
    private String RP_QA_TABLE_NAME = "readingpassage_questionanswer";
    private String RP_QA_COLUMN_RPREFID = "rp_id";
    private String RP_QA_COLUMN_QAREFID = "qa_id";

    ///  bảng liên kết bảng question answer và answers
    private String QA_A_TABLE_NAME = "questionanswer_answers";
    private String QA_A_COLUMN_QAREFID = "qa_id";
    private String A_COLUMN_AREFID = "a_id";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
        this.context = context.getApplicationContext();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createRPTable = "create table " + READINGPASSAGE_TABLE_NAME + " ("
                + READINGPASSAGE_COLUMN_ID + " integer primary key, "
                + READINGPASSAGE_COLUMN_PASSAGE + " text"
                + ")";

        String createQATable = "create table " + QA_TABLE_NAME + " ("
                + QA_COLUMN_ID + " integer primary key, "
                + QA_COLUMN_QUESTION + " text, "
                + QA_COLUMN_CORRECTANSWER + " integer"
                + ")";

        String createAnswerTable = "create table " + ANSWER_TABLE_NAME + " ("
                + ANSWER_COLUMN_ID + " integer primary key, "
                + ANSWER_COLUMN_DEDICATEDID + " integer, "
                + ANSWER_DETAIL + " text"
                + ")";

        String createRP_QATable = "create table " + RP_QA_TABLE_NAME + " ("
                + RP_QA_COLUMN_RPREFID + " integer, "
                + RP_QA_COLUMN_QAREFID + " integer"
                + ")";

        String createQA_ATable = "create table " + QA_A_TABLE_NAME + " ("
                + QA_A_COLUMN_QAREFID + " integer, "
                + A_COLUMN_AREFID + " integer"
                + ")";

        db.execSQL(createRPTable);
        db.execSQL(createQATable);
        db.execSQL(createAnswerTable);

        db.execSQL(createRP_QATable);
        db.execSQL(createQA_ATable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + READINGPASSAGE_TABLE_NAME);
        db.execSQL("drop table if exists " + QA_TABLE_NAME);
        db.execSQL("drop table if exists " + ANSWER_TABLE_NAME);

        db.execSQL("drop table if exists " + RP_QA_TABLE_NAME);
        db.execSQL("drop table if exists " + QA_A_TABLE_NAME);
        onCreate(db);
    }

    //  một số phương thức giao tiếp với sqlite
    public long insertReadingPassge(String passage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(READINGPASSAGE_COLUMN_PASSAGE, passage);
        long id = db.insert(READINGPASSAGE_TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public long insertQuestionAnswer(String question, int correctAnswerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(QA_COLUMN_QUESTION, question);
        values.put(QA_COLUMN_CORRECTANSWER, correctAnswerId);
        long id = db.insert(QA_TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public long insertAnswer(int dedicatedId, String detail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ANSWER_COLUMN_DEDICATEDID, dedicatedId);
        values.put(ANSWER_DETAIL, detail);
        long id = db.insert(ANSWER_TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public long insertRPQA(long rpId, long qaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RP_QA_COLUMN_RPREFID, rpId);
        values.put(RP_QA_COLUMN_QAREFID, qaId);
        long id = db.insert(RP_QA_TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public long insertQAA(long qaId, long answerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(QA_A_COLUMN_QAREFID, qaId);
        values.put(A_COLUMN_AREFID, answerId);
        long id = db.insert(QA_A_TABLE_NAME, null, values);
        db.close();
        return id;
    }

}
