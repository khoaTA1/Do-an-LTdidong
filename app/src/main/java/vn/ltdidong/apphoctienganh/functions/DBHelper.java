package vn.ltdidong.apphoctienganh.functions;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.ltdidong.apphoctienganh.models.QuestionAnswer;
import vn.ltdidong.apphoctienganh.models.ReadingPassage;

public class DBHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "CSDL";

    // bảng cho chế độ trắc nghiệm trong kỹ năng đọc
    ///  bảng reading pasage
    private String READINGPASSAGE_TABLE_NAME = "readingpassage";
    private String READINGPASSAGE_COLUMN_ID = "id";
    private String READINGPASSAGE_COLUMN_PASSAGE = "passage";

    ///  bảng question answer
    private String QA_TABLE_NAME = "questionanswer";
    private String QA_COLUMN_ID = "id";
    private String QA_COLUMN_PREFID = "p_id";
    private String QA_COLUMN_QUESTION = "pasage";
    private String QA_COLUMN_CORRECTANSWER = "correctanswer";

    ///  bảng answers
    private String ANSWER_TABLE_NAME = "answers";
    private String ANSWER_COLUMN_ID = "id";
    private String ANSWER_COLUMN_QREFID = "q_id";
    private String ANSWER_COLUMN_DEDICATEDID = "dedicated_id";
    private String ANSWER_DETAIL = "detail";

    /// bảng liên kết bảng reading passage và question answer
    private String RP_QA_TABLE_NAME = "readingpassage_questionanswer";
    private String RP_QA_COLUMN_RPREFID = "rp_id";
    private String RP_QA_COLUMN_QAREFID = "qa_id";

    ///  bảng liên kết bảng question answer và answers
    private String QA_A_TABLE_NAME = "questionanswer_answers";
    private String QA_A_COLUMN_QAREFID = "qa_id";
    private String QA_A_COLUMN_AREFID = "a_id";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 3);
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
                + QA_COLUMN_PREFID + " integer, "
                + QA_COLUMN_QUESTION + " text, "
                + QA_COLUMN_CORRECTANSWER + " integer"
                + ")";

        String createAnswerTable = "create table " + ANSWER_TABLE_NAME + " ("
                + ANSWER_COLUMN_ID + " integer primary key, "
                + ANSWER_COLUMN_DEDICATEDID + " integer, "
                + ANSWER_COLUMN_QREFID + " integer, "
                + ANSWER_DETAIL + " text"
                + ")";

        String createRP_QATable = "create table " + RP_QA_TABLE_NAME + " ("
                + RP_QA_COLUMN_RPREFID + " integer, "
                + RP_QA_COLUMN_QAREFID + " integer"
                + ")";

        String createQA_ATable = "create table " + QA_A_TABLE_NAME + " ("
                + QA_A_COLUMN_QAREFID + " integer, "
                + QA_A_COLUMN_AREFID + " integer"
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
    public int insertRPList(List<ReadingPassage> RPList) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            for (ReadingPassage rp : RPList) {
                ContentValues rp_value = new ContentValues();
                //ContentValues rp_qa_value = new ContentValues();

                rp_value.put(READINGPASSAGE_COLUMN_ID, rp.getId());
                rp_value.put(READINGPASSAGE_COLUMN_PASSAGE, rp.getPassage());
                Log.d(">>> SQLite", "Id: " + rp.getId() + ", passage: " + rp.getPassage());
                db.insert(READINGPASSAGE_TABLE_NAME, null, rp_value);

                for (QuestionAnswer qa : rp.getQAList()) {
                    ContentValues qa_value = new ContentValues();
                    //ContentValues qa_a_value = new ContentValues();
                    //long answerId;

                    qa_value.put(QA_COLUMN_ID, qa.getId());
                    qa_value.put(QA_COLUMN_QUESTION, qa.getQuestionContent());
                    qa_value.put(QA_COLUMN_CORRECTANSWER, qa.getCorrectAnswer());
                    qa_value.put(QA_COLUMN_PREFID, rp.getId());
                    db.insert(QA_TABLE_NAME, null, qa_value);

                    for (Map.Entry<Integer, String> entry : qa.getAnswers().entrySet()) {
                        ContentValues a_value = new ContentValues();

                        a_value.put(ANSWER_DETAIL, entry.getValue());
                        a_value.put(ANSWER_COLUMN_DEDICATEDID, entry.getKey());
                        a_value.put(ANSWER_COLUMN_QREFID, qa.getId());
                        db.insert(ANSWER_TABLE_NAME, null, a_value);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("!!! SQLite", "Lỗi: ", e);
            return 0;
        }

        return 1;
    }

    public ReadingPassage getReadingPassageById(long passageId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ReadingPassage returnReadingPassage = new ReadingPassage();

        // lấy nội dung đoạn văn trước tiên
        Cursor cursor = db.query(
                READINGPASSAGE_TABLE_NAME,                  // Tên bảng
                null,                      // Cột muốn lấy, null = tất cả
                READINGPASSAGE_COLUMN_ID + " = ?",                  // WHERE clause
                new String[]{String.valueOf(passageId)}, // Các tham số cho ?
                null,                      // groupBy
                null,                      // having
                null                       // orderBy
        );

        if (cursor != null && cursor.moveToFirst()) {
            returnReadingPassage.setId(cursor.getInt(cursor.getColumnIndex(READINGPASSAGE_COLUMN_ID)));
            returnReadingPassage.setPassage(cursor.getString(cursor.getColumnIndex(READINGPASSAGE_COLUMN_PASSAGE)));
            Log.d(">>> SQLite", "Đã tìm được đoạn ngẫu nhiên: " + returnReadingPassage.getId());
        }

        // lấy câu hỏi thuộc đoạn văn hiện tại
        cursor = db.query(
                QA_TABLE_NAME,
                null,
                QA_COLUMN_PREFID + " = ?",
                new String[]{String.valueOf(passageId)},
                null,
                null,
                null
        );

        List<QuestionAnswer> QAList = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    QuestionAnswer qa = new QuestionAnswer();

                    qa.setId(cursor.getInt(cursor.getColumnIndex(QA_COLUMN_ID)));
                    qa.setPassageId(cursor.getInt(cursor.getColumnIndex(QA_COLUMN_PREFID)));
                    qa.setQuestionContent(cursor.getString(cursor.getColumnIndex(QA_COLUMN_QUESTION)));
                    qa.setCorrectAnswer(cursor.getInt(cursor.getColumnIndex(QA_COLUMN_CORRECTANSWER)));

                    // lấy các câu trả lời thuộc câu hỏi hiện tại
                    Cursor cursor2 = db.query(
                            ANSWER_TABLE_NAME,
                            null,
                            ANSWER_COLUMN_QREFID + " = ?",
                            new String[]{String.valueOf(qa.getId())},
                            null,
                            null,
                            null
                    );

                    Map<Integer, String> answers = new HashMap<>();
                    if (cursor2 != null && cursor2.moveToFirst()) {
                        do {
                            answers.put(cursor2.getInt(cursor2.getColumnIndex(ANSWER_COLUMN_DEDICATEDID)),
                                    cursor2.getString(cursor2.getColumnIndex(ANSWER_DETAIL)));
                        } while (cursor2.moveToNext());

                        cursor2.close();
                    }

                    qa.setAnswers(answers);

                    QAList.add(qa);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        returnReadingPassage.setQAList(QAList);

        return returnReadingPassage;
    }

    public long getCountPassage() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + READINGPASSAGE_TABLE_NAME, null);

        long count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();

        return count;
    }

    // hàm lấy danh sách id các đoạn văn trong trường hợp nó không liên tục
    public List<Integer> getAllPassageIds() {
        List<Integer> ids = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(READINGPASSAGE_TABLE_NAME,
                    new String[]{READINGPASSAGE_COLUMN_ID},
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(READINGPASSAGE_COLUMN_ID));
                    Log.d(">>> SQLite", "Thêm id: " + id);
                    ids.add(id);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("!!! SQLite", "Lỗi khi lấy danh sách id", e);
        } finally {
            if (cursor != null) cursor.close();
            // db.close(); // không nên đóng DB ở đây nếu gọi nhiều lần
        }

        return ids;
    }

    public void clearAllTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + READINGPASSAGE_TABLE_NAME + ";");
        db.execSQL("DELETE FROM " + QA_TABLE_NAME + ";");
        db.execSQL("DELETE FROM " + ANSWER_TABLE_NAME + ";");

        db.close();

        Log.d(">>> SQLite", "Xóa cache SQLite");
    }
}
