<template>
    <footer
        class="base-footer"
        :class="{
            'sidebar-hidden': sidebarStore.isHidden,
            'sidebar-collapsed': sidebarStore.isCollapsed
        }"
    >
        <div>Copyright &copy; 2025 QST.</div>
        <div>JAVA 2</div>
    </footer>
</template>

<script setup>
import { useSidebarStore } from '@/stores/sidebar'

const sidebarStore = useSidebarStore()
</script>

<style scoped>
.base-footer {
    display: flex;
    flex-flow: row nowrap;
    align-items: center;
    justify-content: space-between;

    box-sizing: border-box;
    border-top: 0.0625rem solid #cccccc;
    color: #666666;
    font-size: 0.75rem;
    height: 3rem;
    line-height: 2.5rem;

    padding: 0rem 1.25rem;
    position: fixed;
    bottom: 0rem;
    left: 220px; /* 默认侧边栏展开宽度 */
    width: calc(100% - 220px);
    transition: left 0.3s ease, width 0.3s ease;
}

/* 侧边栏隐藏时 */
.base-footer.sidebar-hidden {
    left: 0;
    width: 100%;
}

/* 侧边栏折叠时 */
.base-footer.sidebar-collapsed {
    left: 60px; /* 折叠宽度 */
    width: calc(100% - 60px);
}

/* 侧边栏隐藏且折叠时 */
.base-footer.sidebar-hidden.sidebar-collapsed {
    left: 0;
    width: 100%;
}
</style>
