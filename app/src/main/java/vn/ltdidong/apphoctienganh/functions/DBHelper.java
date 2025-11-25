package vn.ltdidong.apphoctienganh.functions;

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
    private String ANSWER_COLUMN_INTERID = "inter_id";
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
                + ANSWER_COLUMN_INTERID + " integer, "
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
}
