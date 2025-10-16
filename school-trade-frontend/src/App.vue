<template>
    <div id="app">
      <router-view :key="$route.fullPath"></router-view>
    </div>
</template>
<style>
    html, body, #app{
        overflow: visible;
        background-color: #f6f6f6;
    }
</style>
<!-- App.vue -->
<style>
:root{
  --nav: #0c1240;
  --line: #3ba7ff;
  --muted: #e5e7eb;
}

.my-global-message {
  border-radius: 10px !important;
  background: #e5e7eb !important;
  color: #0c1240 !important;
  border: 1px solid #e5e7eb !important;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}
</style>