<template>
  <SpecialLayout @ok="ok">
    <span>Ein Backup wird gerade aufgespielt.</span>
    <span>Bitte warten Sie einen Moment.</span>
  </SpecialLayout>
</template>

<script>

  import SpecialLayout from "../../components/SpecialLayout";

  export default {
    name: 'Restoring',
    components: {SpecialLayout},
    methods: {
      async ok() {
        await this.$emit('update-info');
        await this.$router.push('/');
      }
    }
  }
</script>