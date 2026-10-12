<template>
  <v-container class="fill-height py-12">
    <v-row justify="center" align="center">
      <v-col cols="12" md="8" lg="6">
        <div class="text-center mb-8">
          <h1 class="text-h3 font-weight-bold mb-2">İletişime Geçin</h1>
          <p class="text-subtitle-1 text-grey-darken-1">
            Sorularınız, önerileriniz veya iş birliği için bana ulaşabilirsiniz.
          </p>
        </div>

        <v-card class="pa-8" elevation="4" rounded="lg">
          <v-form @submit.prevent="submitForm" ref="form">
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="formData.name"
                  label="Adınız Soyadınız"
                  variant="outlined"
                  prepend-inner-icon="mdi-account"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="formData.email"
                  label="E-posta Adresiniz"
                  type="email"
                  variant="outlined"
                  prepend-inner-icon="mdi-email"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-textarea
                  v-model="formData.message"
                  label="Mesajınız"
                  variant="outlined"
                  prepend-inner-icon="mdi-message-text"
                  rows="4"
                  required
                ></v-textarea>
              </v-col>
            </v-row>
            
            <v-alert v-if="successMessage" type="success" class="mb-4" closable @click:close="successMessage = ''">
              {{ successMessage }}
            </v-alert>
            <v-alert v-if="errorMessage" type="error" class="mb-4" closable @click:close="errorMessage = ''">
              {{ errorMessage }}
            </v-alert>

            <v-btn
              type="submit"
              color="primary"
              size="large"
              block
              class="mt-2 text-none font-weight-bold"
              :loading="loading"
              elevation="2"
            >
              Mesajı Gönder
            </v-btn>
          </v-form>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, reactive } from 'vue';
import api from '@/services/api';

const formData = reactive({
  name: '',
  email: '',
  message: ''
});

const loading = ref(false);
const successMessage = ref('');
const errorMessage = ref('');

const submitForm = async () => {
  loading.value = true;
  successMessage.value = '';
  errorMessage.value = '';

  try {
    await api.sendMessage(formData);
    successMessage.value = 'Mesajınız başarıyla gönderildi!';
    // Formu temizle
    formData.name = '';
    formData.email = '';
    formData.message = '';
  } catch (error) {
    console.error('Mesaj gönderilirken hata:', error);
    errorMessage.value = 'Mesaj gönderilirken bir hata oluştu. Lütfen tekrar deneyin.';
  } finally {
    loading.value = false;
  }
};
</script>
