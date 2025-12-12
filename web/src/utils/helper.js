export const buildURL = (filename) => new URL(filename, import.meta.env.APP_BASE_URL).href

// 用户头像：/avatars/.png
export const buildAvatarURL = (filename) => buildURL(filename)

// 主题背景：/themes/.png
export const buildThemeURL = (filename) => buildURL(filename)

// AI审核图片：/ai-images/.png
export const buildImageURL = (filename) => buildURL(filename)
