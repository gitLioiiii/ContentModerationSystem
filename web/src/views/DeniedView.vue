<template>
    <div class="p-6 space-y-4">
        <h1 class="italic font-thin text-red-950 text-2xl">About Us!</h1>
        <h1 class="italic text-lg">关于我们</h1>
        <p class="mt-4 text-orange-500" >我们是评分越高说明,风险等级越高，评分越靠近100分匹配到色情，暴力，政治的事件越接近</p>
        <div class="text-orange-500 space-y-2">
            <p class="font-semibold">技术栈：</p>
            <ul class="list-inside space-y-1 ml-4">
                <li><strong>前端：</strong>Vue 3 + Vue Router + Element-Plus + Scss  + Echarts + Axios + Pinia + Tailwind CSS</li>
                <li><strong>后端：</strong>Spring Boot + Spring Security + JWT + Spring Ai </li>
                <li>JavaCv + Springboot自带线程池 + Spring Data MongoDB</li>
                <li><strong>AI审核：</strong>Ollama (本地语言模型) + qwen3-vl:235b-cloud</li>
                <li><strong>数据库：</strong>MogonDB</li>
            </ul>
            <p class="italic text-sm mt-2">头像，图片，视频抽帧保存目录：D:\Project\upload</p>
            <p class="italic text-sm mt-2">qwen3-vl:235b-cloud云端token显示 https://ollama.com/settings</p>
        </div>
        <time class="mt-2 block text-indigo-500 " datetime="9:37" >2025年12月20日</time>
    </div>
</template>
