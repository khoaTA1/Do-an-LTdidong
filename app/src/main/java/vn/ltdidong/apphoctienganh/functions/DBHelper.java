package vn.ltdidong.apphoctienganh.functions;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import vn.ltdidong.apphoctienganh.models.ClozeTestQA;
import vn.ltdidong.apphoctienganh.models.QuestionAnswer;
import vn.ltdidong.apphoctienganh.models.ReadingPassage;
import vn.ltdidong.apphoctienganh.models.Word;
import vn.ltdidong.apphoctienganh.models.WordEntry;

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

    // bảng cloze test QA
    private String CLOZETEST_QA_TABLE_NAME = "clozetest_qa";
    private String CLOZETEST_QA_COLUMN_ID = "id";
    private String CLOZETEST_QA_COLUMN_QUESTION = "question";
    private String CLOZETEST_QA_COLUMN_ANSWER = "answer";

    // bảng theo dõi thói quen tìm từ vựng của người dùng
    public static final String HISTORY_SEARCH_TABLE_NAME = "search_history";
    public static final String HISTORY_SEARCH_COLUMN_ID = "id";
    public static final String HISTORY_SEARCH_COLUMN_WORD = "word";
    public static final String HISTORY_SEARCH_COLUM_TIMESTAMP = "timestamp";
    public static final String HISTORY_SEARCH_COLUM_POS = "pos";
    public static final String HISTORY_SEARCH_COLUM_SYNONYM = "synonym";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 5);
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

        String createCTQATable = "create table " + CLOZETEST_QA_TABLE_NAME + " ("
                + CLOZETEST_QA_COLUMN_ID + " integer primary key, "
                + CLOZETEST_QA_COLUMN_QUESTION + " text, "
                + CLOZETEST_QA_COLUMN_ANSWER + " text"
                + ")";

        String createHSTable = "create table " + HISTORY_SEARCH_TABLE_NAME + " ("
                + HISTORY_SEARCH_COLUMN_ID + " integer primary key AUTOINCREMENT, "
                + HISTORY_SEARCH_COLUMN_WORD + " text, "
                + HISTORY_SEARCH_COLUM_TIMESTAMP + " integer, "
                + HISTORY_SEARCH_COLUM_POS + " text, "
                + HISTORY_SEARCH_COLUM_SYNONYM + " text"
                + ")";

        db.execSQL(createRPTable);
        db.execSQL(createQATable);
        db.execSQL(createAnswerTable);
        db.execSQL(createCTQATable);
        db.execSQL(createHSTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + READINGPASSAGE_TABLE_NAME);
        db.execSQL("drop table if exists " + QA_TABLE_NAME);
        db.execSQL("drop table if exists " + ANSWER_TABLE_NAME);
        db.execSQL("drop table if exists " + CLOZETEST_QA_TABLE_NAME);
        db.execSQL("drop table if exists " + HISTORY_SEARCH_TABLE_NAME);
        onCreate(db);
    }

    //  một số phương thức giao tiếp với sqlite cho mode reading comprehension
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
    public List<Long> getAllPassageIds() {
        List<Long> ids = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(READINGPASSAGE_TABLE_NAME,
                    new String[]{READINGPASSAGE_COLUMN_ID},
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(READINGPASSAGE_COLUMN_ID));
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
        db.execSQL("DELETE FROM " + CLOZETEST_QA_TABLE_NAME + ";");

        // db.close(); // không nên đóng DB ở đây nếu gọi nhiều lần

        Log.d(">>> SQLite", "Xóa cache SQLite");
    }

    //  một số phương thức giao tiếp với sqlite cho mode cloze test
    public int insertCTQAList(List<ClozeTestQA> QAList) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            for (ClozeTestQA qa : QAList) {
                ContentValues cv = new ContentValues();

                cv.put(CLOZETEST_QA_COLUMN_ID, qa.getId());
                cv.put(CLOZETEST_QA_COLUMN_QUESTION, qa.getQuestion());
                cv.put(CLOZETEST_QA_COLUMN_ANSWER, qa.getAnswer());

                db.insert(CLOZETEST_QA_TABLE_NAME, null, cv);
                Log.d(">>> SQLite", "Đã thêm 1 cloze test QA");
            }
        } catch (Exception e) {
            Log.e("!!! SQLite", "Lỗi: ", e);
            return 0;
        }

        return 1;
    }
    public List<Long> getAllCLozeTestQAIds() {
        List<Long> ids = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(CLOZETEST_QA_TABLE_NAME,
                    new String[]{CLOZETEST_QA_COLUMN_ID},
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(CLOZETEST_QA_COLUMN_ID));
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
    public ClozeTestQA getCTQAById(long qaid) {
        SQLiteDatabase db = this.getReadableDatabase();
        ClozeTestQA qa = new ClozeTestQA();
        Cursor cursor;

        try {
            cursor = db.query(
                    CLOZETEST_QA_TABLE_NAME,
                    null,
                    CLOZETEST_QA_COLUMN_ID + " = ?",
                    new String[]{String.valueOf(qaid)},
                    null,
                    null,
                    null
                    );

            if (cursor != null && cursor.moveToFirst()) {
                qa.setId(cursor.getLong(cursor.getColumnIndex(CLOZETEST_QA_COLUMN_ID)));
                qa.setQuestion(cursor.getString(cursor.getColumnIndex(CLOZETEST_QA_COLUMN_QUESTION)));
                qa.setAnswer(cursor.getString(cursor.getColumnIndex(CLOZETEST_QA_COLUMN_ANSWER)));

                Log.d(">>> SQLite", "Đã tìm được cloze test QA theo id: " + qa.getId());
            } else {
                Log.e("!!! SQLite", "KHÔNG tìm được cloze test QA");
                return null;
            }

            return qa;
        } catch (Exception e) {
            Log.e("!!! SQlite", "Lỗi:",e);
        }

        return null;
    }

    // một số phương thức sqlite cho tính năng gợi ý từ mới
    public void saveSearchWord(WordEntry wordEntry) {
        SQLiteDatabase db = this.getWritableDatabase();

        // parse String word từ word entry và danh sách part of speech, synonym
        String word = wordEntry.getWord();
        List<WordEntry.Meaning> meanings = wordEntry.getMeanings();

        List<String> posList = new ArrayList<>();
        List<String> synList = new ArrayList<>();

        // Lấy POS
        for (WordEntry.Meaning m : wordEntry.getMeanings()) {
            posList.add(m.getPartOfSpeech());
        }

        // Lấy synonyms
        for (WordEntry.Meaning m : wordEntry.getMeanings()) {
            if (m.getSynonyms() != null)
                synList.addAll(m.getSynonyms());

            if (m.getDefinitions() != null) {
                for (WordEntry.Definition d : m.getDefinitions()) {
                    if (d.getSynonyms() != null)
                        synList.addAll(d.getSynonyms());
                }
            }
        }

        // Loại trùng synonym
        Set<String> uniqueSyn = new LinkedHashSet<>(synList);
        List<String> finalSynList = new ArrayList<>(uniqueSyn);

        // chuyển trành chuỗi Json để lưu vào SQLite
        Gson gson = new Gson();

        String posJson = gson.toJson(posList);
        String synJson = gson.toJson(finalSynList);

        // insert hoặc update từ vào SQLite
        // nếu từ đã tồn tại thì cập nhật timestamp
        Cursor c = null;
        try {
            c = db.rawQuery(
                    "SELECT id FROM " + HISTORY_SEARCH_TABLE_NAME +
                            " WHERE word = ?", new String[]{word});

            ContentValues cv = new ContentValues();
            cv.put(HISTORY_SEARCH_COLUMN_WORD, word);
            cv.put(HISTORY_SEARCH_COLUM_TIMESTAMP, System.currentTimeMillis());
            cv.put(HISTORY_SEARCH_COLUM_POS, posJson);
            cv.put(HISTORY_SEARCH_COLUM_SYNONYM, synJson);

            if (c.moveToFirst()) {
                db.update(HISTORY_SEARCH_TABLE_NAME, cv, "word=?", new String[]{word});
            } else {
                db.insert(HISTORY_SEARCH_TABLE_NAME, null, cv);
            }
        } finally {
            if (c != null) c.close();
            // db.close(); // không nên đóng DB ở đây nếu gọi nhiều lần
        }
    }

    public List<Word> getRecentWords(int limit) {
        List<Word> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = null;

        try {
            c = db.rawQuery(
                    "SELECT " + HISTORY_SEARCH_COLUMN_WORD + ", " + HISTORY_SEARCH_COLUM_POS + ", "
                            + HISTORY_SEARCH_COLUM_SYNONYM + " FROM " + HISTORY_SEARCH_TABLE_NAME +
                            " ORDER BY " + HISTORY_SEARCH_COLUM_TIMESTAMP + " DESC LIMIT " + limit, null);

            if (c.moveToFirst()) {
                do {
                    String word = c.getString(c.getColumnIndex(HISTORY_SEARCH_COLUMN_WORD));
                    String pos = c.getString(c.getColumnIndex(HISTORY_SEARCH_COLUM_POS));
                    String syn = c.getString(c.getColumnIndex(HISTORY_SEARCH_COLUM_SYNONYM));

                    Gson gson = new Gson();
                    List<String> posList = gson.fromJson(pos, new TypeToken<List<String>>(){}.getType());
                    List<String> synList = gson.fromJson(syn, new TypeToken<List<String>>(){}.getType());

                    Word w = new Word(word, posList, synList);

                    list.add(w);
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) c.close();
            // db.close(); // không nên đóng DB ở đây nếu gọi nhiều lần
        }

        return list;
    }
}
