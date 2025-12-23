// 用户表
db.runCommand({
  collMod: "user",
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["username", "password"],
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
          enum: ["user", "admin"],
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
          enum: ["man", "woman", "none"],
          description: "性别:男/女/未知"
        },
        birthday: {
          bsonType: ["date", "null"],
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
					bsonType: ["object", "null"],
					required: ["city"],
					properties: {
						city: {
							bsonType: "string",
							maxLength: 50,
							description: "城市"
						},
						province: {
							bsonType: ["string", "null"],
							maxLength: 50,
							description: "省份（可选）"
						}
					},
					description: "用户地理位置"
				},
        status: {
          enum: ["active", "ban"],
          description: "账户状态:active活跃/ban封号"
        },
        deletedAt: {
          bsonType: ["date", "null"],
          description: "移除于"
        },
        registeredAt: {
          bsonType: "date",
          description: "注册于"
        }
      }
    }
  }
})
// 用户表索引
db.user.createIndex({ username: 1 }, { unique: true })
db.user.createIndex({ email: 1 }, { unique: true, sparse: true })
db.user.createIndex({ phone: 1 }, { unique: true, sparse: true })
db.user.createIndex({ status: 1 })
db.user.createIndex({ role: 1 })
db.user.createIndex({ registeredAt: -1 })

// 作品内容表
db.runCommand({
  collMod:"content",
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["userId", "status", "createdAt"],
      properties: {
        userId: {
          bsonType: "string",
          description: "发布者的用户ID"
        },
        // 作品信息（用于视频作品发布场景）
        title: {
          bsonType: ["string", "null"],
          maxLength: 100,
          description: "作品标题"
        },
        description: {
          bsonType: ["string", "null"],
          maxLength: 500,
          description: "作品描述"
        },
        coverUrl: {
          bsonType: ["string", "null"],
          maxLength: 500,
          description: "作品封面图片URL"
        },
        videoUrl: {
          bsonType: ["string", "null"],
          maxLength: 500,
          description: "视频文件URL"
        },
        author: {
          bsonType: ["string", "null"],
          maxLength: 50,
          description: "作者名称"
        },
        authorAvatar: {
          bsonType: ["string", "null"],
          maxLength: 255,
          description: "发布作品时的作者头像"
        },
        location: {
          bsonType: ["object", "null"],
          properties: {
            province: {
              bsonType: ["string", "null"],
              maxLength: 50,
              description: "省份"
            },
            city: {
              bsonType: ["string", "null"],
              maxLength: 50,
              description: "城市"
            }
          },
          description: "作品发布地理位置"
        },
        status: {
          enum: ["pending", "approved", "rejected", "reviewing", "manual_rejected", "appealing", "appeal_rejected"],
          description: "审核状态 pending:待AI审核/approved:审核通过/rejected:AI审核不通过/reviewing:人工审核中/manual_rejected:人工审核不通过/appealing:申诉中/appeal_rejected:申诉驳回"
        },
        appealStatus: {
          enum: ["none", "appealing", "approved", "rejected"],
          description: "申诉状态 none:未申诉/appealing:申诉中/approved:申诉通过/rejected:申诉驳回"
        },
        violationCount: {
          bsonType: "int",
          minimum: 0,
          description: "累计违规次数"
        },
        createdAt: {
          bsonType: "date",
          description: "创建时间"
        },
        deletedAt: {
          bsonType: ["date", "null"],
          description: "删除时间"
        }
      }
    }
  }
})
// 索引
db.content.createIndex({ userId: 1, createdAt: -1 })
db.content.createIndex({ status: 1, createdAt: -1 })
db.content.createIndex({ type: 1 })



// 对作品AI自动审核表
  db.createCollection("auto_review_forworks", {
    validator: {
      $jsonSchema: {
        bsonType: "object",
        required: ["contentId", "reviewResults", "Status",
  "reviewedAt"],
        properties: {
          contentId: {
            bsonType: "objectId",
            description: "关联作品的ID（content集合中的_id）"        
          },
          reviewResults: {
            bsonType: "object",
            required: ["textReview"],
            description: "各项审核结果",
            properties: {
              // 文本审核（标题+描述）
              textReview: {
                bsonType: "object",
                required: ["result", "reason", "riskLevel"],
                description: "文本内容审核结果（标题和描述）",       
                properties: {
                  result: {
                    enum: ["通过", "拒绝", "需人工审核"],
                    description: "文本审核结果"
                  },
                  reason: {
                    bsonType: "string",
                    description: "审核理由说明"
                  },
                  riskLevel: {
                    enum: ["低", "中", "高"],
                    description: "风险等级"
                  },
                  sensitiveWords: {
                    bsonType: ["array", "null"],
                    items: { bsonType: "string" },
                    description: "命中的敏感词列表"
                  },
                  processingTime: {
                    bsonType: ["int", "null"],
                    minimum: 0,
                    description: "处理耗时(毫秒)"
                  }
                }
              },
              // 图片审核（封面）
              imageReview: {
                bsonType: ["object", "null"],
                description: "封面图片审核结果（如果有封面）",       
                properties: {
                  result: {
                    enum: ["通过", "不通过", "人工审核"],
                    description: "图片审核结果"
                  },
                  reason: {
                    bsonType: "string",
                    description: "审核理由说明"
                  },
                  matchScore: {
                    bsonType: "double",
                    minimum: 0.0,
                    maximum: 1.0,
                    description: "违规匹配分数(0-1，越高越危险)"     
                  },
                  processingTime: {
                    bsonType: ["int", "null"],
                    minimum: 0,
                    description: "处理耗时(毫秒)"
                  }
                }
              },
              // 视频审核
              videoReview: {
                bsonType: ["object", "null"],
                description: "视频内容审核结果（如果有视频）",       
                properties: {
                  result: {
                    enum: ["通过", "不通过", "人工审核"],
                    description: "视频审核结果"
                  },
                  reason: {
                    bsonType: "string",
                    description: "审核理由说明"
                  },
                  Count: {
                    bsonType: ["int", "null"],
                    minimum: 0,
                    description: "视频抽帧总数"
                  },
                  riskyCount: {
                    bsonType: ["int", "null"],
                    minimum: 0,
                    description: "检测到的风险帧数量"
                  },
                  maxScore: {
                    bsonType: ["double", "null"],
                    minimum: 0.0,
                    maximum: 1.0,
                    description: "所有帧中的最高风险分数"
                  },
                  riskyList: {
                    bsonType: ["array", "null"],
                    description: "风险帧详情列表",
                    items: {
                      bsonType: "object",
                      properties: {
                        frameIndex: {
                          bsonType: "int",
                          description: "帧索引"
                        },
                        timestamp: {
                          bsonType: "double",
                          description: "时间戳(秒)"
                        },
                        score: {
                          bsonType: "double",
                          minimum: 0.0,
                          maximum: 1.0,
                          description: "该帧的风险分数"
                        },
                        reason: {
                          bsonType: ["string", "null"],
                          description: "该帧的风险原因"
                        }
                      }
                    }
                  },
                  processingTime: {
                    bsonType: ["int", "null"],
                    minimum: 0,
                    description: "处理耗时(秒)"
                  }
                }
              }
            }
          },
          Status: {
            enum: ["approved", "rejected", "reviewing"],
            description:
  "审核状态：approved通过/rejected驳回/reviewing需人工审核"
          },
          finalProcessingTime: {
            bsonType: ["int", "null"],
            minimum: 0,
            description: "作品审核处理耗时(秒)"
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
  // 创建索引
  db.auto_review_forworks.createIndex({ contentId: 1 }, { unique:true });
  db.auto_review_forworks.createIndex({ Status: 1, reviewedAt: -1 });
  db.auto_review_forworks.createIndex({ reviewedAt: -1 });


// 人工审核表
db.runCommand({
  collMod: "manual_review",
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["contentId", "reviewerId", "decision", "reviewedAt"],
      properties: {
        contentId: {
          bsonType: "objectId",
          description: "关联作品ID"
        },
        workTitle: {
          bsonType: ["string", "null"],
          maxLength: 100,
          description: "作品标题"
        },
        username: {
          bsonType: ["string", "null"],
          maxLength: 16,
          description: "发布者用户名"
        },
        userId: {
          bsonType: ["objectId", "null"],
          description: "发布者用户ID"
        },
        reviewerId: {
          bsonType: "objectId",
          description: "审核员ID"
        },
        reviewerName: {
          bsonType: ["string", "null"],
          maxLength: 50,
          description: "审核员姓名"
        },
        decision: {
          enum: ["pass", "reject"],
          description: "审核决定：pass通过/reject拒绝"
        },
        reason: {
          bsonType: ["string", "null"],
          maxLength: 500,
          description: "审核意见或驳回理由"
        },
        reviewType: {
          enum: ["first_review", "appeal_review"],
          description: "审核类型：first_review首次人工审核/appeal_review申诉审核"
        },
        appealReason: {
          bsonType: ["string", "null"],
          maxLength: 500,
          description: "申诉理由(当reviewType为appeal_review时填写)"
        },
        originalStatus: {
          enum: ["rejected", "manual_rejected", null],
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
})
// 索引
db.manual_review.createIndex({ contentId: 1, reviewedAt: -1 })
db.manual_review.createIndex({ reviewerId: 1, reviewedAt: -1 })
db.manual_review.createIndex({ decision: 1 })
db.manual_review.createIndex({ reviewType: 1, reviewedAt: -1 })
db.manual_review.createIndex({ userId: 1, reviewedAt: -1 })

// 敏感词库表
db.createCollection("sensitive_words", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["word", "level", "createdAt"],
      properties: {
        word: {
          bsonType: "string", 
          maxLength: 100,
          description: "敏感词"
        },
        level: {
          enum: ["low", "medium", "high"],
          description: "严重程度:低/中/高"
        },
        category: {
				  /* 色情，暴力，政治，垃圾邮件 其他*/
          enum: ["porn", "violence", "political", "spam_mail", "advertising", "other"],
          description: "敏感词分类"
        },
        Effective: {
          bsonType: "bool",
          description: "是否生效(true生效/false未生效)"
        },
        createdAt: {
          bsonType: "date",
          description: "创建时间"
        }
      }
    }
  }
})
db.sensitive_words.createIndex({ word: 1 }, { unique: true })
db.sensitive_words.createIndex({ Effective: 1, level: 1 })
db.sensitive_words.createIndex({ category: 1 })

// 更新敏感词库表语句
db.runCommand({
    collMod: "sensitive_words",
    validator: {
      $jsonSchema: {
        bsonType: "object",
        required: ["word", "level", "createdAt"],
        properties: {
          word: {
            bsonType: "string",
            maxLength: 100,
            description: "敏感词"
          },
          level: {
            enum: ["low", "medium", "high"],
            description: "严重程度:低/中/高"
          },
          category: {
            /* 色情，暴力，政治，垃圾邮件，广告，其他*/     
            enum: ["porn", "violence", "political",
  "spam_mail", "advertising", "other"],
            description: "敏感词分类"
          },
          effective: {
            bsonType: "bool",
            description:
  "是否生效(true生效/false未生效)"
          },
          createdAt: {
            bsonType: "date",
            description: "创建时间"
          }
        }
      }
    }
})
