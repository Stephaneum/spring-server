<template>
    <div style="display: flex; flex-direction: column; align-items: center; justify-content: center">
        <img :src="require('../../assets/img/tech/'+img)" :style="{ 'width': width+'px' }">
        <p style="margin-top: 30px; font-size: 1.1rem; background-color: #dcedc8; border-radius: 20px; text-align: center; padding: 5px 20px 5px 20px">{{ title }}</p>
    </div>
</template>

<script>
export default {
    name: 'Tech',
    props: ['img', 'width', 'title', 'info']
}
</script>