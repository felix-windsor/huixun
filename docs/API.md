# API 文档（草稿）

## 认证与用户
- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/users/me`

## 课程
- `GET /api/courses`
- `POST /api/courses`
- `PUT /api/courses/:id`
- `DELETE /api/courses/:id`

## 文档与内容
- `POST /api/documents/upload`
- `GET /api/documents/:id/status`
- `GET /api/fragments?documentId=...`

## 题库与试题
- `POST /api/quizzes`
- `GET /api/quizzes/:id`
- `POST /api/quizzes/:id/generate`
- `POST /api/questions`
- `GET /api/questions`
- `PUT /api/questions/:id`
- `DELETE /api/questions/:id`

## 学员评估
- `POST /api/attempts`
- `PUT /api/attempts/:id/submit`
- `GET /api/attempts/:id/report`

## 分析
- `GET /api/analytics/overview`
- `GET /api/analytics/knowledge-points`（可选 `quizId` 参数）
