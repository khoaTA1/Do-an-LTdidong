# Hướng dẫn Chức năng "Nghe và Điền vào Chỗ Trống" (Fill Blank)

## 📋 Tổng quan

Chức năng **"Điền chỗ trống"** trong phần Listening cho phép người dùng:
- Nghe audio bài học
- Điền từ còn thiếu vào các chỗ trống trong câu
- Nhận phản hồi về câu trả lời đúng/sai
- Xem kết quả tổng thể sau khi hoàn thành

## 🔧 Cấu trúc Code (Giống với mode Listening cơ bản)

### 1. Model: FillBlankQuestion
File: `app/src/main/java/vn/ltdidong/apphoctienganh/models/FillBlankQuestion.java`

```java
@Entity(tableName = "fill_blank_questions")
public class FillBlankQuestion {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int lessonId;                    // ID bài học
    private String sentenceWithBlanks;       // Câu có chỗ trống, VD: "I wake up at {blank} every day."
    private String correctAnswers;           // Đáp án đúng, VD: "7 AM"
    private String hint;                     // Gợi ý cho người dùng
    private int orderIndex;                  // Thứ tự câu hỏi
    private String audioUrl;                 // URL audio riêng (optional)
}
```

### 2. Repository: FillBlankRepository
File: `app/src/main/java/vn/ltdidong/apphoctienganh/repositories/FillBlankRepository.java`

**Repository Pattern** - Tương tự như `FirebaseListeningRepo`:
```java
public class FillBlankRepository {
    public LiveData<List<FillBlankQuestion>> getFillBlankQuestionsByLesson(int lessonId) {
        // Load từ Firebase Firestore
    }
}
```

### 3. Activity: FillBlankActivity
File: `app/src/main/java/vn/ltdidong/apphoctienganh/activities/FillBlankActivity.java`

**Load dữ liệu từ Firebase:**
```java
private void loadQuestionsFromFirebase(int lessonId) {
    repository.getFillBlankQuestionsByLesson(lessonId).observe(this, questions -> {
        // Xử lý dữ liệu
    });
}
```

## 🔥 Cấu trúc Firebase (Theo cấu trúc của bạn)

### Collection Structure trên Firestore:
```
fill_blank_lesson_listening (collection)  ← Collection riêng cho Fill Blank
├── 1 (document)                           ← Document ID = Lesson ID
│   ├── audioUrl: "blob:https://..."
│   ├── title: "Going Camping"
│   └── questions (subcollection)          ← Subcollection chứa câu hỏi
│       ├── 1 (document)                   ← Document ID tùy ý
│       │   ├── sentenceWithBlanks: "The Bright {blank} went camping..."
│       │   ├── correctAnswers: "family"
│       │   ├── hint: "father, mother and brother"
│       │   ├── audioUrl: "https://..."
│       │   └── orderIndex: 1              ← Thứ tự câu hỏi
│       └── 2 (document)
│           ├── sentenceWithBlanks: "They set up their {blank}..."
│           ├── correctAnswers: "tent"
│           ├── hint: "..."
│           ├── audioUrl: "https://..."
│           └── orderIndex: 2
│
├── 2 (document)
│   └── questions (subcollection)
└── ...
```

## 📝 Hướng dẫn Thêm Dữ liệu THỦ CÔNG trên Firebase

### Bước 1: Mở Firebase Console
1. Truy cập: https://console.firebase.google.com/
2. Chọn project của bạn
3. Vào **Firestore Database**

### Bước 2: Tạo Collection `fill_blank_lesson_listening`

1. Click **"Start collection"** (nếu chưa có)
2. Collection ID: **`fill_blank_lesson_listening`**
3. Click "Next"

### Bước 3: Tạo Document cho Bài học

**Document ID:** Nhập số ID bài học (VD: `1`, `2`, `3`...)

**Fields bài học:**
```
audioUrl: "blob:https://..." (String)
title: "Going Camping" (String)
```

Click **"Save"**

### Bước 4: Thêm Subcollection `questions`

1. Trong document bài học vừa tạo, click **"Start collection"**
2. Collection ID: **`questions`**
3. Click "Next"

### Bước 5: Thêm Document cho Câu hỏi

**Document ID:** Nhập số thứ tự (VD: `1`, `2`, `3`...) hoặc để trống

**Fields:** Nhập các trường sau

| Field Name | Type | Value | Ví dụ |
|------------|------|-------|-------|
| `sentenceWithBlanks` | string | Câu có chỗ trống | `"I wake up at {blank} every day."` |
| `correctAnswers` | string | Đáp án đúng | `"7 AM"` |
| `hint` | string | Gợi ý | `"What time? (Format: number + AM/PM)"` |
| `audioUrl` | string | URL audio cho câu hỏi | `"https://..."` (optional) |
| `orderIndex` | number | Thứ tự câu hỏi | `1` |

4. Click **"Save"**

### Bước 6: Thêm Nhiều Câu hỏi

Lặp lại **Bước 5** để thêm câu hỏi 2, 3, 4...

**Lưu ý:**
- `orderIndex` tăng dần: 1, 2, 3, 4...
- `{blank}` là placeholder cho chỗ trống (BẮT BUỘC phải có trong `sentenceWithBlanks`)
- `audioTimestamp` là thời điểm trong audio mà câu hỏi này xuất hiện (tính bằng giây)

## 💡 Ví dụ Dữ liệu Hoàn chỉnh

### Lesson 2: "Daily Routine"

**Subcollection** `listening_lessons/lesson_2/fill_blank_questions`:

#### Question 1:
```
sentenceWithBlanks: "I wake up at {blank} every day."
correctAnswers: "7 AM"
hint: "What time? (Format: number + AM/PM)"
audioUrl: "https://..." (optional)
orderIndex: 1
```

#### Question 2:
```
sentenceWithBlanks: "First, I {blank} and take a shower."
correctAnswers: "brush my teeth"
hint: "What do you do first in the morning?"
audioUrl: "https://..." (optional)
orderIndex: 2
```

#### Question 3:
```
sentenceWithBlanks: "Then I have {blank} with my family."
correctAnswers: "breakfast"
hint: "What meal do you eat in the morning?"
audioUrl: "https://..." (optional)
orderIndex: 3
```

#### Question 4:
```
sentenceWithBlanks: "After that, I go to {blank} at 8 AM."
correctAnswers: "school"
hint: "Where do students go?"
audioUrl: "https://..." (optional)
orderIndex: 4
```

## 📊 Quy tắc Dữ liệu

### 1. sentenceWithBlanks
- **Bắt buộc:** Phải có ít nhất một `{blank}`
- **Format:** Câu văn tiếng Anh + `{blank}` ở vị trí cần điền
- **Ví dụ:** 
  - ✅ `"I wake up at {blank} every day."`
  - ✅ `"She likes {blank} and {blank}."` (nhiều chỗ trống)
  - ❌ `"I wake up at _____ every day."` (SAI - phải dùng `{blank}`)

### 2. correctAnswers
- **Format:** Đáp án đúng (không phân biệt hoa thường khi so sánh)
- **Nhiều đáp án:** Cách nhau bởi `|` nếu có nhiều đáp án được chấp nhận
- **Ví dụ:**
  - Một đáp án: `"7 AM"`
  - Nhiều đáp án: `"apple|banana|orange"` (chấp nhận bất kỳ đáp án nào)
  
### 3. orderIndex (BẮT BUỘC)
- **Bắt đầu từ:** 1
- **Tăng dần:** 1, 2, 3, 4...
- **Công dụng:** 
  - ✅ Sắp xếp thứ tự hiển thị câu hỏi (câu nào có orderIndex nhỏ hơn sẽ hiện trước)
  - ✅ App sẽ load câu hỏi theo thứ tự orderIndex tăng dần
  - ❌ Nếu thiếu hoặc sai thứ tự, câu hỏi sẽ hiện lung tung
- **Ví dụ:** Câu hỏi 1 có orderIndex=1, câu hỏi 2 có orderIndex=2...

### 4. audioUrl (OPTIONAL)
- **Type:** String (URL)
- **Công dụng:** URL audio riêng cho từng câu hỏi (nếu mỗi câu có audio riêng)
- **Ví dụ:** `"https://firebasestorage.googleapis.com/.../question1.mp3"`

### 5. hint
- **Optional:** Có thể để trống
- **Nên có:** Giúp người dùng dễ trả lời hơn
- **Ví dụ:** `"What time?"`, `"Type of fruit"`

## 🎯 Hình ảnh Minh họa

### Trên Firebase Console:

```
listening_lessons/
  └── lesson_2/                           ← Document bài học
      ├── id: 2
      ├── title: "Daily Routine"
      ├── audioUrl: "https://..."
      └── fill_blank_questions/           ← Subcollection (Click để mở)
          ├── [auto-id-1]/                ← Document câu hỏi 1
          │   ├── sentenceWithBlanks: "I wake up at {blank} every day."
          │   ├── correctAnswers: "7 AM"
          │   ├── hint: "What time?"
          │   ├── orderIndex: 1
          │   └── audioTimestamp: 0
          │
          ├── [auto-id-2]/                ← Document câu hỏi 2
          │   ├── sentenceWithBlanks: "First, I {blank}..."
          │   └── ...
          └── ...
```

## ✅ Checklist Kiểm tra

Sau khi thêm dữ liệu, đảm bảo:

- [ ] Subcollection tên chính xác là `questions`
- [ ] Mỗi câu hỏi có đủ các fields bắt buộc: `sentenceWithBlanks`, `correctAnswers`, `hint`, `orderIndex`
- [ ] `orderIndex` tăng dần từ 1
- [ ] Mỗi `sentenceWithBlanks` có ít nhất một `{blank}`
- [ ] Type của `orderIndex` là **number** (không phải string)
- [ ] Type của `sentenceWithBlanks`, `correctAnswers`, `hint`, `audioUrl` là **string**

## 🧪 Test App

1. **Mở app** → Skill Home → Listening
2. **Chọn mode** "Điền chỗ trống"
3. **Chọn bài học** đã thêm câu hỏi
4. **Kiểm tra:**
   - Câu hỏi có load từ Firebase không?
   - Số lượng câu hỏi đúng chưa?
   - Thứ tự câu hỏi đúng chưa?
   - Audio có play được không?

## 🐛 Xử lý Lỗi

### Lỗi: "Chưa có dữ liệu trên Firebase"
**Nguyên nhân:**
- Subcollection chưa tạo hoặc tên sai
- Document bài học không tồn tại
- Không có câu hỏi nào trong subcollection

**Giải pháp:**
1. Kiểm tra tên subcollection: phải là `fill_blank_questions` (chính xác)
2. Kiểm tra lesson ID có khớp không
3. Đảm bảo đã thêm ít nhất 1 câu hỏi vào subcollection

### Lỗi: "Permission denied"
**Nguyên nhân:** Firestore rules chặn truy cập

**Giải pháp:** Cập nhật Firestore Rules:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /listening_lessons/{lesson} {
      allow read: if true;  // Cho phép đọc tất cả
      
      match /fill_blank_questions/{question} {
        allow read: if true;  // Cho phép đọc subcollection
      }
    }
  }
}
```

### Lỗi: "Thứ tự câu hỏi bị sai"
**Nguyên nhân:** `orderIndex` không đúng hoặc không phải số

**Giải pháp:**
1. Kiểm tra type của `orderIndex` phải là **number**
2. Đảm bảo `orderIndex` tăng dần: 1, 2, 3, 4...

## 📚 Tham khảo

- File dữ liệu mẫu: `firebase-data/fill_blank_questions_data.json`
- Code Repository: `FillBlankRepository.java`
- Code Activity: `FillBlankActivity.java`

---

## 🎉 Hoàn tất!

Sau khi thêm dữ liệu trên Firebase, app sẽ tự động load về và hiển thị. Không cần code thêm gì nữa!

**Lưu ý:** Nếu không có dữ liệu trên Firebase, app sẽ tự động dùng dữ liệu mẫu có sẵn trong code.

### Bước 1: Mở Firebase Console
1. Truy cập: https://console.firebase.google.com/
2. Chọn project của bạn
3. Vào **Firestore Database** (hoặc **Realtime Database**)

### Bước 2: Tạo Collection và Document (Firestore)

#### 2.1. Tạo hoặc Mở Collection `listening_lessons`
- Nếu chưa có, click **"Start collection"**
- Nhập tên collection: `listening_lessons`

#### 2.2. Thêm Document cho Bài học
Click **"Add document"** và nhập:

**Document ID:** `lesson_1` (hoặc để tự động)

**Fields:**
```
id: 1 (Number)
title: "Daily Routine" (String)
description: "Listen about someone's daily activities" (String)
difficulty: "EASY" (String)
audioUrl: "https://firebasestorage.googleapis.com/.../daily_routine.mp3" (String)
duration: 60 (Number)
transcript: "I wake up at 7 AM every day..." (String)
imageUrl: "ic_lesson_2" (String)
questionCount: 3 (Number)
```

#### 2.3. Thêm Subcollection `fill_blank_questions`
1. Trong document bài học vừa tạo, click **"Add collection"**
2. Collection ID: `fill_blank_questions`

#### 2.4. Thêm Document cho Câu hỏi Fill Blank
Click **"Add document"** và nhập:

**Document ID:** Để tự động

**Fields:**
```
sentenceWithBlanks: "I wake up at {blank} every day." (String)
correctAnswers: "7 AM" (String)
hint: "What time? (Format: number + AM/PM)" (String)
orderIndex: 1 (Number)
audioTimestamp: 0 (Number)
```

> **Lưu ý:** `{blank}` là placeholder cho chỗ trống

#### 2.5. Thêm nhiều câu hỏi
Lặp lại bước 2.4 để thêm câu hỏi 2, 3, 4...

### Bước 3: Import Dữ liệu từ File JSON (Nhanh hơn)

Sử dụng file mẫu: `firebase-data/fill_blank_questions_data.json`

#### Cách 1: Dùng Firebase Console (Manual)
1. Mở file JSON
2. Copy từng lesson và paste vào Firebase theo cấu trúc trên

#### Cách 2: Dùng Firebase CLI (Tự động)
```bash
# Cài đặt Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Import dữ liệu (cho Firestore)
firebase firestore:import ./firebase-data --project your-project-id
```

#### Cách 3: Dùng Code để Upload (Khuyên dùng)
Tạo file helper để upload dữ liệu:

```java
public void uploadFillBlankDataToFirebase() {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    
    // Lesson 1
    db.collection("listening_lessons")
        .document("lesson_1")
        .collection("fill_blank_questions")
        .add(new HashMap<String, Object>() {{
            put("sentenceWithBlanks", "I wake up at {blank} every day.");
            put("correctAnswers", "7 AM");
            put("hint", "What time?");
            put("orderIndex", 1);
            put("audioTimestamp", 0);
        }})
        .addOnSuccessListener(documentReference -> 
            Log.d("Upload", "Question added: " + documentReference.getId()))
        .addOnFailureListener(e -> 
            Log.e("Upload", "Error adding question", e));
}
```

## 🎵 Hướng dẫn Upload Audio File

### Bước 1: Mở Firebase Storage
1. Vào Firebase Console
2. Chọn **Storage**
3. Click **"Get started"** nếu chưa kích hoạt

### Bước 2: Tạo Thư mục và Upload File
1. Tạo folder `audio` (nếu chưa có)
2. Click **"Upload file"**
3. Chọn file audio (.mp3, .wav, .m4a...)
4. Đợi upload xong

### Bước 3: Lấy URL của Audio
1. Click vào file vừa upload
2. Click tab **"File location"**
3. Copy **"Download URL"**
   - VD: `https://firebasestorage.googleapis.com/v0/b/project-id.appspot.com/o/audio%2Fdaily_routine.mp3?alt=media&token=abc123...`

### Bước 4: Cập nhật audioUrl trong Firestore
- Paste URL vào field `audioUrl` của document bài học

## 💡 Ví dụ Dữ liệu Hoàn chỉnh

### Lesson: "Daily Routine"

**Firestore Document** (`listening_lessons/lesson_2`):
```json
{
  "id": 2,
  "title": "Daily Routine",
  "description": "Listen about someone's daily activities",
  "difficulty": "EASY",
  "audioUrl": "https://firebasestorage.googleapis.com/.../daily_routine.mp3",
  "duration": 60,
  "transcript": "I wake up at 7 AM every day. First, I brush my teeth...",
  "imageUrl": "ic_lesson_2",
  "questionCount": 4
}
```

**Subcollection** (`listening_lessons/lesson_2/fill_blank_questions`):

**Question 1:**
```json
{
  "sentenceWithBlanks": "I wake up at {blank} every day.",
  "correctAnswers": "7 AM",
  "hint": "What time? (Format: number + AM/PM)",
  "orderIndex": 1,
  "audioTimestamp": 0
}
```

**Question 2:**
```json
{
  "sentenceWithBlanks": "First, I {blank} and take a shower.",
  "correctAnswers": "brush my teeth",
  "hint": "What do you do first in the morning?",
  "orderIndex": 2,
  "audioTimestamp": 5
}
```

**Question 3:**
```json
{
  "sentenceWithBlanks": "Then I have {blank} with my family.",
  "correctAnswers": "breakfast",
  "hint": "What meal do you eat in the morning?",
  "orderIndex": 3,
  "audioTimestamp": 10
}
```

**Question 4:**
```json
{
  "sentenceWithBlanks": "After that, I go to {blank} at 8 AM.",
  "correctAnswers": "school",
  "hint": "Where do students go?",
  "orderIndex": 4,
  "audioTimestamp": 15
}
```

## 🔌 Kết nối Code với Firebase

### File cần chỉnh sửa: FillBlankActivity.java

**Hiện tại (dòng 116-131):**
```java
private void loadData() {
    lesson = (ListeningLesson) getIntent().getSerializableExtra("lesson");
    
    // TODO: Load fill-blank questions from Firebase
    // Tạm thời dùng dữ liệu mẫu
    questions = createSampleQuestions();
    
    if (lesson != null) {
        tvLessonTitle.setText(lesson.getTitle());
    } else {
        tvLessonTitle.setText("Fill in the Blanks");
    }
}
```

**Cần thay đổi thành:**
```java
private void loadData() {
    lesson = (ListeningLesson) getIntent().getSerializableExtra("lesson");
    
    if (lesson != null) {
        tvLessonTitle.setText(lesson.getTitle());
        loadQuestionsFromFirebase(lesson.getId());
    } else {
        tvLessonTitle.setText("Fill in the Blanks");
        questions = createSampleQuestions();
        displayQuestion();
    }
}

private void loadQuestionsFromFirebase(int lessonId) {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    
    progressBar.setVisibility(View.VISIBLE);
    
    db.collection("listening_lessons")
        .whereEqualTo("id", lessonId)
        .limit(1)
        .get()
        .addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentSnapshot lessonDoc = queryDocumentSnapshots.getDocuments().get(0);
                
                // Load subcollection fill_blank_questions
                lessonDoc.getReference()
                    .collection("fill_blank_questions")
                    .orderBy("orderIndex")
                    .get()
                    .addOnSuccessListener(questionsSnapshot -> {
                        questions = new ArrayList<>();
                        
                        for (QueryDocumentSnapshot doc : questionsSnapshot) {
                            FillBlankQuestion q = new FillBlankQuestion();
                            q.setLessonId(lessonId);
                            q.setSentenceWithBlanks(doc.getString("sentenceWithBlanks"));
                            q.setCorrectAnswers(doc.getString("correctAnswers"));
                            q.setHint(doc.getString("hint"));
                            q.setOrderIndex(doc.getLong("orderIndex").intValue());
                            q.setAudioTimestamp(doc.getLong("audioTimestamp").intValue());
                            
                            questions.add(q);
                        }
                        
                        progressBar.setVisibility(View.GONE);
                        
                        if (questions.isEmpty()) {
                            Toast.makeText(this, "Không có câu hỏi cho bài học này", 
                                Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            displayQuestion();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FillBlank", "Error loading questions", e);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Lỗi tải câu hỏi: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                        finish();
                    });
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Không tìm thấy bài học", Toast.LENGTH_SHORT).show();
                finish();
            }
        })
        .addOnFailureListener(e -> {
            Log.e("FillBlank", "Error loading lesson", e);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Lỗi tải bài học: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
            finish();
        });
}
```

## 📊 Quy tắc Dữ liệu

### 1. sentenceWithBlanks
- **Bắt buộc:** Có ít nhất một `{blank}`
- **Format:** Câu văn tiếng Anh + `{blank}` ở vị trí cần điền
- **Ví dụ:** 
  - ✅ "I wake up at {blank} every day."
  - ✅ "She likes {blank} and {blank}."
  - ❌ "I wake up at _____ every day." (sai format)

### 2. correctAnswers
- **Format:** Đáp án đúng (chữ thường/hoa đều được)
- **Nhiều đáp án:** Cách nhau bởi `|`
- **Ví dụ:**
  - Một đáp án: `"7 AM"`
  - Nhiều đáp án: `"apple|banana|orange"`
  
### 3. orderIndex
- **Bắt đầu từ:** 1
- **Tăng dần:** 1, 2, 3, 4...
- **Dùng để:** Sắp xếp thứ tự câu hỏi

### 4. audioTimestamp
- **Đơn vị:** Giây
- **Ví dụ:** 
  - `0` = Bắt đầu audio
  - `5` = 5 giây vào audio
  - `30` = 30 giây vào audio

## ✅ Checklist Kiểm tra

Sau khi thêm dữ liệu, kiểm tra:

- [ ] Collection `listening_lessons` đã có document cho bài học
- [ ] Document bài học có field `audioUrl` hợp lệ
- [ ] Subcollection `fill_blank_questions` đã được tạo
- [ ] Mỗi câu hỏi có đầy đủ 5 fields: sentenceWithBlanks, correctAnswers, hint, orderIndex, audioTimestamp
- [ ] orderIndex tăng dần từ 1
- [ ] Mỗi `sentenceWithBlanks` có ít nhất một `{blank}`
- [ ] Audio file đã upload lên Firebase Storage
- [ ] URL audio có thể truy cập được (test bằng browser)

## 🐛 Xử lý Lỗi Thường gặp

### Lỗi: "Permission denied"
**Nguyên nhân:** Firestore rules chặn truy cập

**Giải pháp:** Cập nhật Firestore Rules:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /listening_lessons/{lesson} {
      allow read: if true;
      allow write: if request.auth != null;
      
      match /fill_blank_questions/{question} {
        allow read: if true;
      }
    }
  }
}
```

### Lỗi: "Audio không phát được"
**Kiểm tra:**
1. URL audio có đúng không?
2. File audio có tồn tại trên Storage không?
3. Storage Rules có cho phép đọc không?

**Storage Rules:**
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /audio/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

### Lỗi: "Không load được câu hỏi"
**Kiểm tra:**
1. Lesson ID có khớp không?
2. Subcollection name có đúng `fill_blank_questions` không?
3. Fields name có đúng không? (case-sensitive)

## 📚 Tài liệu Tham khảo

- [Firebase Firestore Documentation](https://firebase.google.com/docs/firestore)
- [Firebase Storage Documentation](https://firebase.google.com/docs/storage)
- File mẫu: `firebase-data/fill_blank_questions_data.json`
- File mẫu: `listening_lessons_data.json`

## 🎯 Tóm tắt Quy trình

1. **Upload Audio** → Firebase Storage → Lấy URL
2. **Tạo Lesson** → Collection `listening_lessons` → Thêm field `audioUrl`
3. **Tạo Câu hỏi** → Subcollection `fill_blank_questions` → Thêm các câu hỏi
4. **Cập nhật Code** → FillBlankActivity.java → Thêm method `loadQuestionsFromFirebase()`
5. **Test** → Chạy app → Chọn bài học → Kiểm tra chức năng

---

**Lưu ý:** Đảm bảo đã kết nối Firebase với project Android (file `google-services.json`) trước khi thực hiện các bước trên.
