<template>
  <article class="base-article">
    <h1 class="color-red">用户发布的内容面板</h1>
    <div :style="{ height: '2000px' }"></div>
  </article>
</template>


<style lang="scss">
@use "@/styles/element/rootConfig" as *;

.color-red {
  color: $red;
}
</style>
