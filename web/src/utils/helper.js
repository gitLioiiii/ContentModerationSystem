export const buildURL = (filename) => new URL(filename, import.meta.env.APP_BASE_URL).href

// 用户头像 - 后端返回格式：/avatars/xxx.png
export const buildAvatarURL = (filename) => buildURL(filename)
