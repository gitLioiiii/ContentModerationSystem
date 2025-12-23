/*
 Navicat Premium Data Transfer

 Source Server         : localhost27017
 Source Server Type    : MongoDB
 Source Server Version : 80201 (8.2.1)
 Source Host           : localhost:27017
 Source Schema         : content_moderation

 Target Server Type    : MongoDB
 Target Server Version : 80201 (8.2.1)
 File Encoding         : 65001

 Date: 23/12/2025 16:17:23
*/


// ----------------------------
// Collection structure for auto_review_forworks
// ----------------------------
db.getCollection("auto_review_forworks").drop();
db.createCollection("auto_review_forworks");
db.getCollection("auto_review_forworks").createIndex({
    contentId: NumberInt("1")
}, {
    name: "contentId_1",
    unique: true
});
db.getCollection("auto_review_forworks").createIndex({
    Status: NumberInt("1"),
    reviewedAt: NumberInt("-1")
}, {
    name: "Status_1_reviewedAt_-1"
});
db.getCollection("auto_review_forworks").createIndex({
    reviewedAt: NumberInt("-1")
}, {
    name: "reviewedAt_-1"
});

// ----------------------------
// Documents of auto_review_forworks
// ----------------------------
db.getCollection("auto_review_forworks").insert([ {
    _id: ObjectId("694a467f10f1e6cef91a49aa"),
    contentId: "694a457c10f1e6cef91a49a9",
    reviewResults: {
        textReview: {
            result: "需人工审核",
            reason: "文本涉及'军事飞机坠毁'，可能隐含政治敏感信息和暴力血腥内容，但未提供具体细节，无法确认是否违规",
            riskLevel: "中",
            sensitiveWords: [ ],
            processingTime: NumberInt("72")
        },
        imageReview: {
            result: "通过",
            reason: "图片内容为飞机与月牙的创意摄影，无任何色情、暴力或政治敏感元素",
            matchScore: 10,
            processingTime: NumberInt("12")
        },
        videoReview: {
            result: "人工审核",
            reason: "视频存在疑似违规内容，建议人工复审。违规帧数: 2/9，违规率: 22.22%，最高风险分数: 55.00",
            Count: NumberInt("9"),
            riskyCount: NumberInt("2"),
            maxScore: 55,
            riskyList: [
                {
                    frameIndex: NumberInt("1"),
                    timestamp: 3,
                    score: 45,
                    reason: "图片显示疑似导弹发射或军事飞行器活动场景，存在军事相关元素，可能涉及敏感内容需进一步确认"
                },
                {
                    frameIndex: NumberInt("7"),
                    timestamp: 21,
                    score: 55,
                    reason: "图片显示燃烧坠落的飞机及烟雾轨迹，存在暴力场景嫌疑但无法确认是否涉及真实事故或敏感事件"
                }
            ],
            processingTime: NumberInt("171")
        }
    },
    Status: "reviewing",
    finalProcessingTime: NumberInt("255"),
    reviewedAt: ISODate("2025-12-23T07:36:31.019Z"),
    _class: "com.example.demo.entity.WorkAutoReviewEntity"
} ]);
db.getCollection("auto_review_forworks").insert([ {
    _id: ObjectId("694a48b510f1e6cef91a49ac"),
    contentId: "694a478110f1e6cef91a49ab",
    reviewResults: {
        textReview: {
            result: "通过",
            reason: "内容为普通自然景观描述，无任何违规信息",
            riskLevel: "低",
            sensitiveWords: [ ],
            processingTime: NumberInt("125")
        },
        imageReview: {
            result: "通过",
            reason: "图片为自然风景，包含花田、日落和山景，无任何违规内容",
            matchScore: 10,
            processingTime: NumberInt("56")
        },
        videoReview: {
            result: "通过",
            reason: "视频内容健康，无明显违规。违规帧数: 0/6，违规率: 0.00%",
            Count: NumberInt("6"),
            riskyCount: NumberInt("0"),
            maxScore: 0,
            riskyList: [ ],
            processingTime: NumberInt("122")
        }
    },
    Status: "approved",
    finalProcessingTime: NumberInt("304"),
    reviewedAt: ISODate("2025-12-23T07:45:57.582Z"),
    _class: "com.example.demo.entity.WorkAutoReviewEntity"
} ]);
db.getCollection("auto_review_forworks").insert([ {
    _id: ObjectId("694a4af910f1e6cef91a49af"),
    contentId: "694a4a3e10f1e6cef91a49ae",
    reviewResults: {
        textReview: {
            result: "通过",
            reason: "内容仅描述自然现象雾凇，无任何违规信息",
            riskLevel: "低",
            sensitiveWords: [ ],
            processingTime: NumberInt("78")
        },
        imageReview: {
            result: "通过",
            reason: "图片展示雪景城市景观，包含自然风景与地标建筑，无任何违规内容",
            matchScore: 10,
            processingTime: NumberInt("60")
        },
        videoReview: {
            result: "通过",
            reason: "视频内容健康，无明显违规。违规帧数: 0/3，违规率: 0.00%",
            Count: NumberInt("3"),
            riskyCount: NumberInt("0"),
            maxScore: 0,
            riskyList: [ ],
            processingTime: NumberInt("44")
        }
    },
    Status: "approved",
    finalProcessingTime: NumberInt("183"),
    reviewedAt: ISODate("2025-12-23T07:55:37.514Z"),
    _class: "com.example.demo.entity.WorkAutoReviewEntity"
} ]);
db.getCollection("auto_review_forworks").insert([ {
    _id: ObjectId("694a4d4510f1e6cef91a49b2"),
    contentId: "694a4bba10f1e6cef91a49b1",
    reviewResults: {
        textReview: {
            result: "需人工审核",
            reason: "内容包含'军事敏感'关键词，可能涉及政治敏感信息，需人工复审确认",
            riskLevel: "中",
            sensitiveWords: [ ],
            processingTime: NumberInt("34")
        },
        imageReview: {
            result: "通过",
            reason: "图片展示军事装备及自然环境，无色情、暴力或政治敏感内容",
            matchScore: 20,
            processingTime: NumberInt("60")
        },
        videoReview: {
            result: "人工审核",
            reason: "视频存在疑似违规内容，建议人工复审。违规帧数: 3/12，违规率: 25.00%，最高风险分数: 45.00",
            Count: NumberInt("12"),
            riskyCount: NumberInt("3"),
            maxScore: 45,
            riskyList: [
                {
                    frameIndex: NumberInt("1"),
                    timestamp: 2.96963630296964,
                    score: 45,
                    reason: "图片中出现搭载枪械的机械装置，存在潜在暴力元素疑虑；左上角出现'人民日报'标识需确认是否涉及不当关联"
                },
                {
                    frameIndex: NumberInt("6"),
                    timestamp: 17.8178178178178,
                    score: 45,
                    reason: "图片包含'人民日报'水印，需人工确认是否涉及政治敏感关联内容"
                },
                {
                    frameIndex: NumberInt("7"),
                    timestamp: 20.7874541207875,
                    score: 45,
                    reason: "图片中出现疑似枪支的物体，存在安全隐患需进一步确认"
                }
            ],
            processingTime: NumberInt("296")
        }
    },
    Status: "reviewing",
    finalProcessingTime: NumberInt("391"),
    reviewedAt: ISODate("2025-12-23T08:05:25.176Z"),
    _class: "com.example.demo.entity.WorkAutoReviewEntity"
} ]);

// ----------------------------
// Collection structure for content
// ----------------------------
db.getCollection("content").drop();
db.createCollection("content");
db.getCollection("content").createIndex({
    userId: NumberInt("1"),
    createdAt: NumberInt("-1")
}, {
    name: "userId_1_createdAt_-1"
});
db.getCollection("content").createIndex({
    status: NumberInt("1"),
    createdAt: NumberInt("-1")
}, {
    name: "status_1_createdAt_-1"
});
db.getCollection("content").createIndex({
    type: NumberInt("1")
}, {
    name: "type_1"
});

// ----------------------------
// Documents of content
// ----------------------------
db.getCollection("content").insert([ {
    _id: ObjectId("694a457c10f1e6cef91a49a9"),
    userId: "6938e70753706af599a7cd91",
    title: "飞机坠毁",
    coverUrl: "/covers/c34886ea-7376-4eab-802c-14bbae338448.jpg",
    videoUrl: "/videos/c4b783f7-f2c7-41fa-b57f-0ca59caf0dec.mp4",
    description: "军事飞机坠毁",
    authorAvatar: "/avatars/cbbc4ace-b9b6-44b3-85c5-62510f493b91.jpg",
    author: "test",
    location: {
        province: "广东省",
        city: "广州市"
    },
    status: "rejected",
    appealStatus: "none",
    violationCount: NumberInt("0"),
    createdAt: ISODate("2025-12-23T07:32:12.429Z"),
    _class: "com.example.demo.entity.ContentEntity"
} ]);
db.getCollection("content").insert([ {
    _id: ObjectId("694a478110f1e6cef91a49ab"),
    userId: "6938e70753706af599a7cd91",
    title: "花海",
    coverUrl: "/covers/356360a1-b9b7-4297-98cf-66acc76de0bb.jpg",
    videoUrl: "/videos/83669d32-7cff-467d-8962-6b3d3426ddbe.mp4",
    description: "花海视频",
    authorAvatar: "/avatars/cbbc4ace-b9b6-44b3-85c5-62510f493b91.jpg",
    author: "test",
    location: {
        province: "广东省",
        city: "广州市"
    },
    status: "approved",
    appealStatus: "none",
    violationCount: NumberInt("0"),
    createdAt: ISODate("2025-12-23T07:40:49.872Z"),
    _class: "com.example.demo.entity.ContentEntity"
} ]);
db.getCollection("content").insert([ {
    _id: ObjectId("694a4a3e10f1e6cef91a49ae"),
    userId: "6944e9a9e9e6f39dffa3d97e",
    title: "雾凇",
    coverUrl: "/covers/fc53e094-4f0d-47f8-b129-48b733da2a4b.jpg",
    videoUrl: "/videos/16ac87e8-1b68-4021-a4a1-44c3336a1472.mp4",
    description: "这是雾凇视频",
    authorAvatar: "/avatars/e56d8663-9575-460e-a3f3-167ed5adb1fa.jpg",
    author: "test2",
    location: {
        province: "",
        city: ""
    },
    status: "approved",
    appealStatus: "none",
    violationCount: NumberInt("0"),
    createdAt: ISODate("2025-12-23T07:52:30.79Z"),
    _class: "com.example.demo.entity.ContentEntity"
} ]);
db.getCollection("content").insert([ {
    _id: ObjectId("694a4bba10f1e6cef91a49b1"),
    userId: "6938e70753706af599a7cd91",
    title: "军事敏感",
    coverUrl: "/covers/1374f1d6-fb5e-4de3-943a-3eba7f93df4f.jpg",
    videoUrl: "/videos/41852916-a302-4ec7-8236-a2ba9ccb132d.mp4",
    description: "里面有机器狗和士兵",
    authorAvatar: "/avatars/cbbc4ace-b9b6-44b3-85c5-62510f493b91.jpg",
    author: "test",
    location: {
        province: "广东省",
        city: "广州市"
    },
    status: "approved",
    appealStatus: "none",
    violationCount: NumberInt("0"),
    createdAt: ISODate("2025-12-23T07:58:50.848Z"),
    _class: "com.example.demo.entity.ContentEntity"
} ]);

// ----------------------------
// Collection structure for manual_review
// ----------------------------
db.getCollection("manual_review").drop();
db.createCollection("manual_review",{
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: [
                "contentId",
                "reviewerId",
                "decision",
                "reviewedAt"
            ],
            properties: {
                contentId: {
                    bsonType: "objectId",
                    description: "关联作品ID"
                },
                workTitle: {
                    bsonType: [
                        "string",
                        "null"
                    ],
                    maxLength: 100,
                    description: "作品标题"
                },
                username: {
                    bsonType: [
                        "string",
                        "null"
                    ],
                    maxLength: 16,
                    description: "发布者用户名"
                },
                userId: {
                    bsonType: [
                        "objectId",
                        "null"
                    ],
                    description: "发布者用户ID"
                },
                reviewerId: {
                    bsonType: "objectId",
                    description: "审核员ID"
                },
                reviewerName: {
                    bsonType: [
                        "string",
                        "null"
                    ],
                    maxLength: 50,
                    description: "审核员姓名"
                },
                decision: {
                    enum: [
                        "pass",
                        "reject"
                    ],
                    description: "审核决定：pass通过/reject拒绝"
                },
                reason: {
                    bsonType: [
                        "string",
                        "null"
                    ],
                    maxLength: 500,
                    description: "审核意见或驳回理由"
                },
                reviewType: {
                    enum: [
                        "first_review",
                        "appeal_review"
                    ],
                    description: "审核类型：first_review首次人工审核/appeal_review申诉审核"
                },
                appealReason: {
                    bsonType: [
                        "string",
                        "null"
                    ],
                    maxLength: 500,
                    description: "申诉理由(当reviewType为appeal_review时填写)"
                },
                originalStatus: {
                    enum: [
                        "rejected",
                        "manual_rejected",
                        null
                    ],
                    description: "申诉前的原始状态(当reviewType为appeal_review时记录)"
                },
                reviewedAt: {
                    bsonType: "date",
                    description: "审核完成时间"
                }
            }
        }
    },
    validationLevel: "moderate",
    validationAction: "warn"
});
db.getCollection("manual_review").createIndex({
    contentId: NumberInt("1"),
    reviewedAt: NumberInt("-1")
}, {
    name: "contentId_1_reviewedAt_-1"
});
db.getCollection("manual_review").createIndex({
    reviewerId: NumberInt("1"),
    reviewedAt: NumberInt("-1")
}, {
    name: "reviewerId_1_reviewedAt_-1"
});
db.getCollection("manual_review").createIndex({
    decision: NumberInt("1")
}, {
    name: "decision_1"
});
db.getCollection("manual_review").createIndex({
    reviewType: NumberInt("1"),
    reviewedAt: NumberInt("-1")
}, {
    name: "reviewType_1_reviewedAt_-1"
});
db.getCollection("manual_review").createIndex({
    userId: NumberInt("1"),
    reviewedAt: NumberInt("-1")
}, {
    name: "userId_1_reviewedAt_-1"
});

// ----------------------------
// Documents of manual_review
// ----------------------------
db.getCollection("manual_review").insert([ {
    _id: ObjectId("694a4dd710f1e6cef91a49b3"),
    contentId: "694a4bba10f1e6cef91a49b1",
    workTitle: "军事敏感",
    username: "test",
    reviewerId: "6938e70753706af599a7cd91",
    reviewerName: "管理员",
    decision: "pass",
    reason: "",
    reviewType: "first_review",
    reviewedAt: ISODate("2025-12-23T08:07:51.745Z"),
    _class: "com.example.demo.entity.ManualReviewEntity"
} ]);
db.getCollection("manual_review").insert([ {
    _id: ObjectId("694a4e8b10f1e6cef91a49b4"),
    contentId: "694a457c10f1e6cef91a49a9",
    workTitle: "飞机坠毁",
    username: "test",
    reviewerId: "6938e70753706af599a7cd91",
    reviewerName: "房玄龄",
    decision: "reject",
    reason: "",
    reviewType: "first_review",
    reviewedAt: ISODate("2025-12-23T08:10:51.887Z"),
    _class: "com.example.demo.entity.ManualReviewEntity"
} ]);

// ----------------------------
// Collection structure for sensitive_words
// ----------------------------
db.getCollection("sensitive_words").drop();
db.createCollection("sensitive_words",{
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: [
                "word",
                "level",
                "createdAt"
            ],
            properties: {
                word: {
                    bsonType: "string",
                    maxLength: 100,
                    description: "敏感词"
                },
                level: {
                    enum: [
                        "low",
                        "medium",
                        "high"
                    ],
                    description: "严重程度:低/中/高"
                },
                category: {
                    enum: [
                        "porn",
                        "violence",
                        "political",
                        "spam_mail",
                        "advertising",
                        "other"
                    ],
                    description: "敏感词分类"
                },
                effective: {
                    bsonType: "bool",
                    description: "是否生效(true生效/false未生效)"
                },
                createdAt: {
                    bsonType: "date",
                    description: "创建时间"
                }
            }
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("sensitive_words").createIndex({
    word: NumberInt("1")
}, {
    name: "word_1",
    unique: true
});
db.getCollection("sensitive_words").createIndex({
    status: NumberInt("1"),
    level: NumberInt("1")
}, {
    name: "status_1_level_1"
});
db.getCollection("sensitive_words").createIndex({
    category: NumberInt("1")
}, {
    name: "category_1"
});
db.getCollection("sensitive_words").createIndex({
    Effective: NumberInt("1"),
    level: NumberInt("1")
}, {
    name: "Effective_1_level_1"
});

// ----------------------------
// Documents of sensitive_words
// ----------------------------
db.getCollection("sensitive_words").insert([ {
    _id: ObjectId("69391ac363dba97b69539aa5"),
    word: "臭婊子",
    category: "other",
    level: "medium",
    effective: true,
    createdAt: ISODate("2025-12-10T07:01:23.195Z"),
    _class: "com.example.demo.entity.SensitiveWordEntity"
} ]);
db.getCollection("sensitive_words").insert([ {
    _id: ObjectId("69391d4963dba97b69539abd"),
    word: "傻逼",
    category: "other",
    level: "low",
    effective: true,
    createdAt: ISODate("2025-12-10T07:12:09.577Z"),
    _class: "com.example.demo.entity.SensitiveWordEntity"
} ]);
db.getCollection("sensitive_words").insert([ {
    _id: ObjectId("6941096bfa2a0929f9e39d4d"),
    word: "刷单",
    category: "advertising",
    level: "high",
    effective: true,
    createdAt: ISODate("2025-12-16T07:25:31.66Z"),
    _class: "com.example.demo.entity.SensitiveWordEntity"
} ]);
db.getCollection("sensitive_words").insert([ {
    _id: ObjectId("6949f43f10f1e6cef91a4977"),
    word: "催情剂",
    category: "porn",
    level: "high",
    effective: true,
    createdAt: ISODate("2025-12-23T01:45:35.531Z"),
    _class: "com.example.demo.entity.SensitiveWordEntity"
} ]);
db.getCollection("sensitive_words").insert([ {
    _id: ObjectId("6949f44f10f1e6cef91a4978"),
    word: "你好",
    category: "other",
    level: "low",
    effective: false,
    createdAt: ISODate("2025-12-23T01:45:51.504Z"),
    _class: "com.example.demo.entity.SensitiveWordEntity"
} ]);
db.getCollection("sensitive_words").insert([ {
    _id: ObjectId("6949f4ea10f1e6cef91a4979"),
    word: "贪污",
    category: "political",
    level: "high",
    effective: true,
    createdAt: ISODate("2025-12-23T01:48:26.626Z"),
    _class: "com.example.demo.entity.SensitiveWordEntity"
} ]);
db.getCollection("sensitive_words").insert([ {
    _id: ObjectId("6949f51110f1e6cef91a497a"),
    word: "我日你妈",
    category: "porn",
    level: "medium",
    effective: true,
    createdAt: ISODate("2025-12-23T01:49:05.826Z"),
    _class: "com.example.demo.entity.SensitiveWordEntity"
} ]);
db.getCollection("sensitive_words").insert([ {
    _id: ObjectId("6949feed10f1e6cef91a497b"),
    word: "杀你全家",
    category: "violence",
    level: "high",
    effective: true,
    createdAt: ISODate("2025-12-23T02:31:09.522Z"),
    _class: "com.example.demo.entity.SensitiveWordEntity"
} ]);
db.getCollection("sensitive_words").insert([ {
    _id: ObjectId("694a431010f1e6cef91a49a8"),
    word: "中日局势",
    category: "political",
    level: "high",
    effective: true,
    createdAt: ISODate("2025-12-23T07:21:52.838Z"),
    _class: "com.example.demo.entity.SensitiveWordEntity"
} ]);

// ----------------------------
// Collection structure for user
// ----------------------------
db.getCollection("user").drop();
db.createCollection("user",{
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: [
                "username",
                "password"
            ],
            properties: {
                username: {
                    bsonType: "string",
                    maxLength: 16,
                    description: "用户名"
                },
                name: {
                    bsonType: "string",
                    maxLength: 32,
                    description: "姓名"
                },
                password: {
                    bsonType: "string",
                    maxLength: 512,
                    description: "密码"
                },
                role: {
                    enum: [
                        "user",
                        "admin"
                    ],
                    description: "角色:user用户/admin管理员"
                },
                avatar: {
                    bsonType: "string",
                    maxLength: 255,
                    description: "头像路径"
                },
                backgroundImage: {
                    bsonType: "string",
                    maxLength: 255,
                    description: "个人资料背景图片路径"
                },
                gender: {
                    enum: [
                        "man",
                        "woman",
                        "none"
                    ],
                    description: "性别:男/女/未知"
                },
                birthday: {
                    bsonType: [
                        "date",
                        "null"
                    ],
                    description: "生日"
                },
                phone: {
                    bsonType: "string",
                    maxLength: 20,
                    description: "手机号"
                },
                email: {
                    bsonType: "string",
                    maxLength: 100,
                    description: "邮箱"
                },
                location: {
                    bsonType: [
                        "object",
                        "null"
                    ],
                    required: [
                        "city"
                    ],
                    properties: {
                        city: {
                            bsonType: "string",
                            maxLength: 50,
                            description: "城市"
                        },
                        province: {
                            bsonType: [
                                "string",
                                "null"
                            ],
                            maxLength: 50,
                            description: "省份（可选）"
                        }
                    },
                    description: "用户地理位置"
                },
                status: {
                    enum: [
                        "active",
                        "ban"
                    ],
                    description: "账户状态:active活跃/ban封号"
                },
                deletedAt: {
                    bsonType: [
                        "date",
                        "null"
                    ],
                    description: "移除于"
                },
                registeredAt: {
                    bsonType: "date",
                    description: "注册于"
                }
            }
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("user").createIndex({
    username: NumberInt("1")
}, {
    name: "username_1",
    unique: true
});
db.getCollection("user").createIndex({
    email: NumberInt("1")
}, {
    name: "email_1",
    sparse: true
});
db.getCollection("user").createIndex({
    phone: NumberInt("1")
}, {
    name: "phone_1",
    sparse: true
});
db.getCollection("user").createIndex({
    role: NumberInt("1")
}, {
    name: "role_1"
});
db.getCollection("user").createIndex({
    status: NumberInt("1")
}, {
    name: "status_1"
});
db.getCollection("user").createIndex({
    registeredAt: NumberInt("-1")
}, {
    name: "registeredAt_-1"
});

// ----------------------------
// Documents of user
// ----------------------------
db.getCollection("user").insert([ {
    _id: ObjectId("6938e70753706af599a7cd91"),
    username: "test",
    name: "房玄龄",
    password: "{bcrypt}$2a$10$7lbXv.UO7KPCMwihumvDJ.yk6pSQmCsy0vEytYpltjyaJeWQ6J1pG",
    role: "user",
    avatar: "/avatars/cbbc4ace-b9b6-44b3-85c5-62510f493b91.jpg",
    backgroundImage: "/themes/14401529-4dbd-4b76-aeaf-983a3e1e1375.jpg",
    phone: "15723877026",
    email: "3079573272@qq.com",
    birthday: ISODate("2025-12-09T16:00:00.000Z"),
    gender: "man",
    province: "广东省",
    city: "广州市",
    status: "active",
    registeredAt: ISODate("2025-12-10T03:20:39.693Z"),
    _class: "com.example.demo.entity.UserEntity"
} ]);
db.getCollection("user").insert([ {
    _id: ObjectId("694104dcfa2a0929f9e39d44"),
    username: "test3",
    name: "不是管理员",
    password: "{bcrypt}$2a$10$G1pCtA0KY/FrixgzfH21verBAjmrUe0DLtHnkJ.swKeNQjxKa/ijO",
    role: "user",
    avatar: "/avatars/ea8e7406-0341-4b8a-88a1-a9eb56145104.jpg",
    phone: "15722349990",
    email: "3079573272@qq.com",
    birthday: ISODate("2025-12-15T16:00:00.000Z"),
    gender: "none",
    province: "广东省",
    city: "汕头市",
    status: "active",
    registeredAt: ISODate("2025-12-16T07:06:04.204Z"),
    _class: "com.example.demo.entity.UserEntity"
} ]);
db.getCollection("user").insert([ {
    _id: ObjectId("6944c732e9e6f39dffa3d978"),
    username: "admin",
    name: "管理员",
    password: "{bcrypt}$2a$10$kfZ.dBsQwRtxuVq8bAWStuyVlEv4IzFqSm9Bn3Ha8knDcsr2H5SlC",
    role: "admin",
    avatar: "/images/fafef73f-6171-45d2-961f-87abd90affa7.png",
    phone: "15745678999",
    email: "3079573272@qq.com",
    birthday: ISODate("2025-12-18T16:00:00.000Z"),
    gender: "man",
    province: "广东省",
    city: "江门市",
    status: "active",
    registeredAt: ISODate("2025-12-19T03:32:02.169Z"),
    _class: "com.example.demo.entity.UserEntity"
} ]);
db.getCollection("user").insert([ {
    _id: ObjectId("6944e9a9e9e6f39dffa3d97e"),
    username: "test2",
    name: "瀚海",
    password: "{bcrypt}$2a$10$3oSkKpIaN/px9xdzLU.BsuH5y.Ze4vDQrXo3Kv2SR0TeVssj9oH.i",
    role: "user",
    avatar: "/avatars/e56d8663-9575-460e-a3f3-167ed5adb1fa.jpg",
    phone: "",
    email: "",
    gender: "none",
    province: "天津市",
    city: "河东区",
    status: "active",
    registeredAt: ISODate("2025-12-19T05:59:05.154Z"),
    _class: "com.example.demo.entity.UserEntity"
} ]);
