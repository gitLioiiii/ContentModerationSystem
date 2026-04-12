<template>
    <div class="file-uploader">
        <div class="file-uploader-preview" v-if="files.length > 0">
            <div class="file-uploader-preview-item" v-for="(file, index) in files" :key="index">
                <slot :file="file" name="default">
                    <a :href="buildURL(file.filename)" target="_blank">{{
                        file.originalFilename
                    }}</a>
                </slot>
            </div>
        </div>
        <div
            v-if="props.limit > 0 && props.limit > files.length"
            class="file-uploader-trigger"
            @click.stop="upload"
        >
            <slot name="trigger">
                <button type="button">上传</button>
            </slot>
        </div>
    </div>
</template>

<script setup>
import request from '@/utils/request'
import { buildURL } from '@/utils/helper'

const props = defineProps({
    limit: {
        type: Number,
        default: 0,
    },
    uploadPath: {
        type: String,
        default: 'images', // 默认上传到 images 目录
    },
})

const files = defineModel({ type: Array, default: [] })

const upload = () => {
    // <input type="file" />
    var fileInput = document.createElement('input')
    fileInput.setAttribute('type', 'file')
    fileInput.addEventListener('input', (e) => {
        // console.log(e.target.files)
        if (e.target.files.length > 0) {
            var formData = new FormData()
            // formData.append('key', 'value')
            // <input type="file" name="file" />
            formData.append('file', e.target.files[0], e.target.files[0]['name'])
            // 图片保存路径为D:\Project\upload\images
            // request.post('/upload/images', formData).then((response) => {
            // 使用动态上传路径
            request.post(`/upload/${props.uploadPath}`, formData).then((response) => {
                if (response.data.status === true) {
                    // console.log(response.data)
                    files.value.push(response.data.payload)
                }
            })
        }
    })
    fileInput.click()
}
</script>

<style>
.file-uploader-preview {
    display: flex;
    flex-flow: row nowrap;
    align-items: stretch;
    justify-content: flex-start;
}

.file-uploader-preview-item {
    border: 0.0625rem dotted #dddddd;
    margin: 0rem 0rem 1rem 0rem;
    padding: 0.5rem;
}

.file-uploader-preview-item + .file-uploader-preview-item {
    margin-left: 1rem;
}
</style>
