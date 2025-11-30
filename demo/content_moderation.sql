// 修改
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

// 内容表
db.createCollection("content", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["userId", "contentItems", "status", "createdAt"],
      properties: {
        userId: {
          bsonType: "objectId",
          description: "发布者的用户ID"
        },
        contentItems: {
          bsonType: "array",
          minItems: 1,
          description: "内容项数组，支持5种场景：1.纯文本 2.纯图片 3.纯视频 4.文本+图片 5.文本+视频",
          items: {
            bsonType: "object",
            required: ["type", "order"],
            properties: {
              type: {
                enum: ["text", "image", "video"],
                description: "内容项类型：text文本/image图片/video视频"
              },
              order: {
                bsonType: "int",
                minimum: 0,
                description: "内容项的显示顺序"
              },
              textContent: {
                bsonType: ["string", "null"],
                maxLength: 5000,
                description: "文本内容（当type为text时必填）"
              },
              mediaUrl: {
                bsonType: ["string", "null"],
                maxLength: 500,
                description: "媒体文件URL（当type为image或video时必填）"
              },
              thumbnailUrl: {
                bsonType: ["string", "null"],
                maxLength: 500,
                description: "缩略图URL（视频时可选）"
              },
              duration: {
                bsonType: ["int", "null"],
                minimum: 0,
                description: "视频时长（秒，视频时可选）"
              },
              width: {
                bsonType: ["int", "null"],
                minimum: 0,
                description: "媒体宽度（像素，图片/视频时可选）"
              },
              height: {
                bsonType: ["int", "null"],
                minimum: 0,
                description: "媒体高度（像素，图片/视频时可选）"
              }
            }
          }
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
					description: "用户发布地理位置"
				},
        status: {
          enum: ["pending", "passed", "rejected", "reviewing"],
          description: "审核状态 待审核/已通过/已驳回/人工审核中"
        },
        appealStatus: {
          enum: ["none", "appealing", "approved", "rejected"],
          description: "申诉状态 未申诉/申诉中/申诉通过/申诉驳回"
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
        publishedAt: {
          bsonType: ["date", "null"],
          description: "发布时间"
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


// AI自动审核
db.createCollection("auto_review", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["contentId", "itemReviews", "overallScore", "overallRiskLevel", "reviewedAt"],
      properties: {
        contentId: {
          bsonType: "objectId",
          description: "关联内容ID"
        },
        itemReviews: {
          bsonType: "array",
          minItems: 1,
          description: "每个内容项的审核结果",
          items: {
            bsonType: "object",
            required: ["itemOrder", "itemType", "score", "riskLevel"],
            properties: {
              itemOrder: {
                bsonType: "int",
                minimum: 0,
                description: "对应contentItems中的order"
              },
              itemType: {
                enum: ["text", "image", "video"],
                description: "内容项类型"
              },
              score: {
                bsonType: "number",
                minimum: 0,
                maximum: 100,
                description: "该内容项的审核得分 (0-100)"
              },
              riskLevel: {
                enum: ["safe", "suspected", "risky"],
                description: "该内容项的风险等级：安全/疑似/高风险"
              },
              keywords: {
                bsonType: ["array", "null"],
                items: { bsonType: "string" },
                description: "命中的敏感词（文本类型）"
              },
              violationTypes: {
                bsonType: ["array", "null"],
                /* 敏感词，色情，暴力，政治，垃圾邮件 */
                items: {
                  enum: ["sensitive_words", "porn", "violence", "political", "spam_mail"]
                },
                description: "违规类型：敏感词/色情/暴力/政治/垃圾邮件"
              },
              details: {
                bsonType: ["object", "null"],
                description: "详细检测结果（视频时间戳、图片区域坐标等）"
              },
              processingTime: {
                bsonType: ["int", "null"],
                minimum: 0,
                description: "该项处理耗时(毫秒)"
              }
            }
          }
        },
        overallScore: {
          bsonType: "number",
          minimum: 0,
          maximum: 100,
          description: "整体审核得分（通常取最低分或加权平均）"
        },
        overallRiskLevel: {
          enum: ["safe", "suspected", "risky"],
          description: "整体风险等级（通常取最高风险级别）"
        },
        totalProcessingTime: {
          bsonType: ["int", "null"],
          minimum: 0,
          description: "总处理耗时(毫秒)"
        },
        reviewedAt: {
          bsonType: "date",
          description: "审核时间"
        }
      }
    }
  }
})

// 索引
db.auto_review.createIndex({ contentId: 1 })
db.auto_review.createIndex({ riskLevel: 1, reviewedAt: -1 })
db.auto_review.createIndex({ reviewedAt: -1 })

// 人工审核
db.createCollection("manual_review", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["contentId", "reviewerId", "decision", "reviewedAt"],
      properties: {
        contentId: {
          bsonType: "objectId",
          description: "关联内容ID"
        },
        reviewerId: {
          bsonType: "objectId",
          description: "审核员ID"
        },
        decision: {
          enum: ["pass", "reject"],
          description: "审核决定：pass通过/reject驳回"
        },
        reason: {
          bsonType: ["string", "null"],
          maxLength: 500,
          description: "驳回理由"
        },
        violatedItems: {
          bsonType: ["array", "null"],
          description: "违规的内容项（如果驳回）",
          items: {
            bsonType: "object",
            properties: {
              itemOrder: {
                bsonType: "int",
                minimum: 0,
                description: "对应contentItems中的order"
              },
              itemType: {
                enum: ["text", "image", "video"],
                description: "内容项类型"
              },
              violationTypes: {
                bsonType: "array",
                /* 敏感词，色情，暴力，政治，垃圾邮件 */
                items: {
                  enum: ["sensitive_words", "porn", "violence", "political", "spam_mail", "other"]
                },
                description: "该项的违规类型"
              },
              note: {
                bsonType: ["string", "null"],
                maxLength: 200,
                description: "针对该项的备注"
              }
            }
          }
        },
        reviewType: {
          enum: ["first_review", "appeal_review"],
          description: "审核类型: first_review首次审核/appeal_review申诉审核"
        },
        reviewedAt: {
          bsonType: "date",
          description: "审核完成时间"
        }
      }
    }
  }
})

// 索引
db.manual_review.createIndex({ contentId: 1, reviewedAt: -1 })
db.manual_review.createIndex({ reviewerId: 1, reviewedAt: -1 })
db.manual_review.createIndex({ decision: 1 })

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
          description: "严重等级:低/中/高"
        },
        matchType: {
          enum: ["exact", "fuzzy", "regex"],
          description: "匹配方式:精确/模糊/正则"
        },
        pattern: {
          bsonType: ["string", "null"],
          description: "正则表达式(当matchType=regex时)"
        },
        category: {
          enum: ["porn", "violence", "political", "spam"],
          description: "所属分类"
        },
        status: {
          enum: ["active", "inactive"],
          description: "启用状态"
        },
        createdBy: {
          bsonType: "objectId",
          description: "创建者ID"
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
db.sensitive_words.createIndex({ status: 1, level: 1 })
db.sensitive_words.createIndex({ category: 1 })


// 审核规则设置
db.createCollection("review_rules", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["ruleName", "thresholds", "createdAt"],
      properties: {
        ruleName: {
          bsonType: "string",
          maxLength: 100,
          description: "规则名称"
        },
        thresholds: {
          bsonType: "object",
          properties: {
            autoPass: {
              bsonType: "number",
              minimum: 0,
              maximum: 100,
              description: "自动通过阈值"
            },
            manualReview: {
              bsonType: "number",
              minimum: 0,
              maximum: 100,
              description: "转人工审核阈值"
            },
            autoReject: {
              bsonType: "number",
              minimum: 0,
              maximum: 100,
              description: "自动驳回阈值"
            }
          },
          description: "审核阈值配置"
        },
        contentType: {
          enum: ["text", "image", "video", "all"],
          description: "适用内容类型"
        },
        punishmentRules: {
          bsonType: "array",
          items: {
            bsonType: "object",
            properties: {
              violationCount: { bsonType: "int" },
              action: {
                enum: ["warning", "restrict", "ban_1day", "ban_7day", "permanent_ban"]
              }
            }
          },
          description: "分级处罚规则"
        },
        status: {
          enum: ["active", "inactive"],
          description: "规则状态"
        },
        createdAt: {
          bsonType: "date",
          description: "创建时间"
        },
        updatedAt: {
          bsonType: ["date", "null"],
          description: "更新时间"
        }
      }
    }
  }
})

db.review_rules.createIndex({ status: 1 })
db.review_rules.createIndex({ contentType: 1 })