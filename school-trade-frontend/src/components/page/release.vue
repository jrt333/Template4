<template>
  <div>
    <app-head />
    <main class="release-idle-container">
      <div class="upload-container">
        <div class="header">
          <h2>📸 List Your Item</h2>
          <p class="subtitle">Share your unused items with the community</p>
        </div>

        <!-- Image Upload Section -->
        <div class="image-upload-section">
          <div class="image-upload-area" @click="$refs.fileInput.click()" :class="{ 'has-image': form.picture_list.length > 0 }">
            <div v-if="imagePreview.length === 0" class="upload-placeholder">
              <div class="upload-icon">📷</div>
              <p>Click to upload images</p>
              <span class="upload-hint">Supports PNG, JPG formats, up to 9 images</span>
            </div>
            <div v-else class="image-preview-container">
              <div v-for="(preview, index) in imagePreview" :key="index" class="image-preview">
                <img :src="preview" :alt="`Preview ${index + 1}`" />
                <div class="image-overlay">
                  <button type="button" @click.stop="removeImage(index)" class="remove-btn">×</button>
                </div>
              </div>
            </div>
          </div>
          <input
              ref="fileInput"
              type="file"
              @change="onFileChange"
              accept="image/*"
              multiple
              style="display: none"
          />
        </div>

        <!-- Listing Type Selection -->
        <div class="listing-type-section">
          <h3 class="section-title">Choose Transaction Type</h3>
          <div class="listing-type-buttons">
            <button
                type="button"
                @click="form.idle_trade = 1"
                :class="['type-btn', { active: form.idle_trade === 1 }]"
            >
              <div class="type-icon">💰</div>
              <div class="type-content">
                <div class="type-title">Sell</div>
                <div class="type-desc">Sell item for money</div>
              </div>
            </button>

            <button
                type="button"
                @click="form.idle_trade = 2"
                :class="['type-btn', { active: form.idle_trade === 2 }]"
            >
              <div class="type-icon">🔄</div>
              <div class="type-content">
                <div class="type-title">Exchange</div>
                <div class="type-desc">Trade with other items</div>
              </div>
            </button>
          </div>
        </div>

        <!-- Form Fields -->
        <div class="form-grid">
          <div class="form-group">
            <label class="form-label">
              <span class="label-text">Item Name</span>
              <span class="required">*</span>
            </label>
            <input
                v-model="form.idle_name"
                type="text"
                class="form-input"
                placeholder="Enter item name"
                maxlength="64"
            />
          </div>

          <!-- Sell Form Fields -->
          <template v-if="form.idle_trade === 1">
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">
                  <span class="label-text">Original Price</span>
                  <span class="required">*</span>
                </label>
                <div class="input-with-prefix">
                  <span class="prefix">$</span>
                  <input
                      v-model="form.idle_original_price"
                      type="number"
                      class="form-input with-prefix"
                      placeholder="0.00"
                      step="0.01"
                      min="0"
                  />
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">
                  <span class="label-text">Current Price</span>
                  <span class="required">*</span>
                </label>
                <div class="input-with-prefix">
                  <span class="prefix">$</span>
                  <input
                      v-model="form.idle_price"
                      type="number"
                      class="form-input with-prefix"
                      placeholder="0.00"
                      step="0.01"
                      min="0"
                  />
                </div>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">
                <span class="label-text">Condition</span>
                <span class="required">*</span>
              </label>
              <select v-model="form.idle_new" class="form-input">
                <option value="">Select condition</option>
                <option value="1">Brand New</option>
                <option value="2">Like New</option>
                <option value="3">Used</option>
              </select>
            </div>
          </template>

          <!-- Exchange Form Fields -->
          <template v-else-if="form.idle_trade === 2">
            <div class="form-group">
              <label class="form-label">
                <span class="label-text">Original Price</span>
                <span class="required">*</span>
              </label>
              <div class="input-with-prefix">
                <span class="prefix">$</span>
                <input
                    v-model="form.idle_original_price"
                    type="number"
                    class="form-input with-prefix"
                    placeholder="0.00"
                    step="0.01"
                    min="0"
                />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">
                <span class="label-text">Condition</span>
                <span class="required">*</span>
              </label>
              <select v-model="form.idle_new" class="form-input">
                <option value="">Select condition</option>
                <option value="1">Brand New</option>
                <option value="2">Like New</option>
                <option value="3">Used</option>
              </select>
            </div>
          </template>

          <!-- Common Fields -->
          <div class="form-group">
            <label class="form-label">
              <span class="label-text">Category</span>
              <span class="required">*</span>
            </label>
            <select v-model="form.idle_label" class="form-input">
              <option value="">Select category</option>
              <option value="univ">University</option>
              <option value="tech">Tech</option>
              <option value="clothes">Clothes</option>
              <option value="sports">Sports</option>
              <option value="living">Living</option>
              <option value="other">Other</option>
            </select>
          </div>

          <div class="form-group">
            <!-- 标签和AI按钮在同一行 -->
            <div class="label-row">
              <label class="form-label">
                <span class="label-text">Item Details</span>
                <span class="required">*</span>
              </label>
              <button
                  type="button"
                  @click="handleEnhanceDetails"
                  :disabled="aiEnhancing || !form.idle_details"
                  class="ai-btn"
              >
                <span v-if="aiEnhancing" class="loading-icon">⏳</span>
                <template v-else>
                  <span>✨</span>
                  <span>🌐</span>
                </template>
                <span>{{ aiEnhancing ? 'Enhancing...' : 'AI Enhance & Translate' }}</span>
              </button>
            </div>
            <textarea
                v-model="form.idle_details"
                class="form-textarea"
                :placeholder="form.idle_trade === 1 ? 'Describe the item condition, features, and other details...' : 'Describe item details and reason for exchange...'"
                rows="4"
                maxlength="2048"
            ></textarea>
            <p class="hint-text">✨ AI will translate to English and enhance your description</p>
          </div>
        </div>


        <!-- Action Buttons -->
        <div class="action-buttons">
          <button @click="goBack" class="btn btn-secondary">← Back</button>

          <el-button
              type="primary"
              native-type="button"
              :loading="uploading || submitting"
              :disabled="uploading || submitting"
              @click="submitForm"
          >
            List for Sale
          </el-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script>

import AppHead from "../common/AppHeader.vue";

const LABEL_MAP = {
  univ:1,
  tech: 2,
  clothes: 3,
  sports: 4,
  living: 5,
  other: 6,
};

export default {
  components: {AppHead},
  data() {
    return {
      form: {
        idle_name: '',
        idle_details: '',
        picture_list: [],
        idle_price: '',
        idle_original_price: '',
        idle_label: '',
        idle_trade: 1,
        idle_new: '',
        exchange_wants: ''
      },
      imagePreview: [],
      uploading: false,
      submitting: false,
      aiEnhancing: false,
      GEMINI_API_KEY: process.env.VUE_APP_GEMINI_API_KEY || ''
    };
  },
  computed: {
    isFormValid() {
      const baseValid =
          this.form.idle_name &&
          this.form.idle_details &&
          this.form.idle_label &&
          this.form.idle_new &&
          this.form.idle_original_price;

      if (this.form.idle_trade === 1) {
        return baseValid && this.form.idle_price;
      } else {
        return baseValid;
      }
    }
  },
  methods: {

    toast(type, msg) {
      if (this.$message && this.$message[type]) this.$message[type](msg);
      else alert(msg);
    },

    async enhanceWithAI(text, fieldType = 'details') {
      if (!text || !text.trim()) {
        this.toast('error', 'Please enter some text first');
        return null;
      }

      this.aiEnhancing = true;

      try {
        console.log('=== AI Enhancement Request ===');
        console.log('Text:', text);
        console.log('Field Type:', fieldType);

        const response = await this.$api.enhanceText({
          text: text,
          fieldType: fieldType
        });

        console.log('=== API Response ===');
        console.log('Full response:', response);

        if (response && response.status_code === 1 && response.data) {
          return response.data.trim();
        } else {
          const errorMsg = response.msg || response.message || 'Unknown error';
          throw new Error(errorMsg);
        }

      } catch (error) {
        console.error('=== AI Enhancement Error ===');
        console.error('Error:', error);

        let errorMsg = 'Unknown error';
        if (error.response && error.response.data) {
          errorMsg = error.response.data.msg || error.response.data.message || error.message;
        } else if (error.message) {
          errorMsg = error.message;
        }

        this.toast('error', 'AI Enhancement failed: ' + errorMsg);
        return null;
      } finally {
        this.aiEnhancing = false;
      }
    },

    async handleEnhanceDetails() {
      const enhanced = await this.enhanceWithAI(this.form.idle_details, 'details');
      if (enhanced) {
        this.form.idle_details = enhanced;
        this.toast('success', 'Description enhanced with AI!');
      }
    },

    onFileChange(e) {
      const files = Array.from(e.target.files || []);

      files.forEach((file) => {
        if (this.form.picture_list.length >= 9) {
          alert('Maximum 9 images allowed');
          return;
        }

        this.form.picture_list.push(file);

        const reader = new FileReader();
        reader.onload = (ev) => {
          this.imagePreview.push(ev.target.result);
        };
        reader.readAsDataURL(file);
      });

      e.target.value = '';
    },

    removeImage(index) {
      this.form.picture_list.splice(index, 1);
      this.imagePreview.splice(index, 1);
    },

    goBack() {
      window.history.back();
    },

    async uploadOneImage(file) {
      var fd = new FormData();
      fd.append('file', file);

      var data = await this.$api.uploadFile(fd);

      if (!data || data.status_code !== 1) {
        throw new Error((data && data.msg) ? data.msg : 'Image Upload Fail');
      }

      var url = '';
      if (data) {
        if (typeof data.data === 'string') {
          url = data.data;
        } else if (data.data && typeof data.data === 'object' && data.data.url) {
          url = data.data.url;
        }
      }

      if (!url) {
        throw new Error('Image Upload Fail');
      }
      return url;
    },

    async uploadAllImages() {
      var files = this.form.picture_list;
      if (!files.length) return [];
      var urls = [];
      for (var i = 0; i < files.length; i++) {
        var u = await this.uploadOneImage(files[i]);
        urls.push(u);
      }
      return urls;
    },

    buildFormData(payload) {
      var fd = new FormData();
      Object.keys(payload).forEach(function (k) {
        var v = payload[k];
        if (v === undefined || v === null) v = '';
        if (Array.isArray(v) || (typeof v === 'object' && v !== null)) {
          fd.append(k, JSON.stringify(v));
        } else {
          fd.append(k, v);
        }
      });
      return fd;
    },

    async submitForm () {
      if (!this.isFormValid) {
        this.toast('error','Please complete item info');
        return;
      }
      if (!this.form.picture_list || !this.form.picture_list.length) {
        this.toast('error','Please Upload At Least One Image');
        return;
      }

      // 验证 Original Price
      if (!this.form.idle_original_price || this.form.idle_original_price <= 0) {
        this.toast('error', 'Please enter a valid Original Price');
        return;
      }

      try {
        const imageUrls = await this.uploadAllImages();

        const payload = {
          idleName: (this.form.idle_name || '').trim(),
          idleDetails: (this.form.idle_details || '').trim(),
          pictureList: JSON.stringify(imageUrls),
          idlePrice: this.form.idle_trade === 1 ? Number(this.form.idle_price) : 0,
          idleOriginalPrice: Number(this.form.idle_original_price) || 0,
          idleLabel: LABEL_MAP[this.form.idle_label] || null,
          idleTrade: Number(this.form.idle_trade),
          idleNew: Number(this.form.idle_new),
          exchangeWants: this.form.idle_trade === 2 ? (this.form.exchange_wants || '').trim() : null,
        };

        console.log('=== Submit Payload ===', payload);

        const data = await this.$api.addIdleItem(payload);

        if (data && data.status_code === 1) {
          this.toast('success', this.form.idle_trade === 1 ? 'Post Success (Sell)' : 'Post Success (Exchange)');
          this.$router && this.$router.push && this.$router.push('/home');
        } else {
          this.toast('error', (data && data.msg) ? data.msg : 'Post Error');
        }
      } catch (e) {
        console.error(e);
        this.toast('error', e && e.message ? e.message : 'Submit Fail');
      }
    }

  }
}

</script>

<style scoped>
.release-idle-container {
  min-height: calc(100vh - 120px);
  background: #f8fafc;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

.upload-container {
  background: white;
  border-radius: 24px;
  padding: 32px;
  width: 100%;
  max-width: 600px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.header {
  text-align: center;
  margin-bottom: 32px;
}

.header h2 {
  font-size: 2rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 8px 0;
}

.subtitle {
  color: #6b7280;
  margin: 0;
  font-size: 1rem;
}

.image-upload-section {
  margin-bottom: 32px;
}

.image-upload-area {
  border: 2px dashed #d1d5db;
  border-radius: 16px;
  padding: 32px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
  position: relative;
  overflow: hidden;
  min-height: 120px;
}

.image-upload-area:hover {
  border-color: #3b82f6;
  background: #eff6ff;
  transform: translateY(-2px);
}

.image-upload-area.has-image {
  border-style: solid;
  border-color: #10b981;
  background: white;
  padding: 16px;
}

.upload-placeholder {
  pointer-events: none;
}

.upload-icon {
  font-size: 3rem;
  margin-bottom: 16px;
  opacity: 0.6;
}

.upload-placeholder p {
  font-size: 1.1rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px 0;
}

.upload-hint {
  color: #9ca3af;
  font-size: 0.875rem;
}

.image-preview-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.image-preview {
  position: relative;
  display: inline-block;
  border-radius: 8px;
  overflow: hidden;
}

.image-preview img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
}

.image-overlay {
  position: absolute;
  top: 4px;
  right: 4px;
}

.remove-btn {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(239, 68, 68, 0.9);
  color: white;
  border: none;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.remove-btn:hover {
  background: rgb(239, 68, 68);
  transform: scale(1.1);
}

.form-grid {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  gap: 12px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
  font-weight: 600;
  color: #374151;
}

.label-row .form-label {
  margin-bottom: 0;
}

.required {
  color: #ef4444;
}

.ai-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.25);
}

.ai-btn:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.35);
}

.ai-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.hint-text {
  font-size: 0.75rem;
  color: #6b7280;
  margin-top: 6px;
}

.input-with-prefix {
  position: relative;
  display: flex;
  align-items: center;
}

.prefix {
  position: absolute;
  left: 16px;
  color: #6b7280;
  font-weight: 600;
  z-index: 1;
  pointer-events: none;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.2s ease;
  background: white;
  box-sizing: border-box;
}

.form-input.with-prefix {
  padding-left: 40px;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input::placeholder {
  color: #9ca3af;
}

.form-textarea {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.2s ease;
  background: white;
  resize: vertical;
  min-height: 100px;
  font-family: inherit;
  box-sizing: border-box;
}

.form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 32px;
  gap: 16px;
}

.btn {
  padding: 14px 28px;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.btn-secondary {
  background: #f3f4f6;
  color: #6b7280;
  border: 2px solid #e5e7eb;
}

.btn-secondary:hover {
  background: #e5e7eb;
  transform: translateY(-2px);
}

.btn-primary {
  background: #3b82f6;
  color: white;
  flex: 1;
  justify-content: center;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(59, 130, 246, 0.25);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.listing-type-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 20px 0;
  text-align: center;
}

.listing-type-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.type-btn {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.type-btn:hover {
  border-color: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

.type-btn.active {
  border-color: #3b82f6;
  background: #eff6ff;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

.type-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.type-content {
  flex: 1;
}

.type-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.type-desc {
  font-size: 0.875rem;
  color: #6b7280;
  line-height: 1.4;
}

@media (max-width: 640px) {
  .release-idle-container {
    padding: 10px;
  }

  .upload-container {
    margin: 0;
    padding: 24px;
    border-radius: 16px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .listing-type-buttons {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column-reverse;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }

  .image-preview-container {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }

  .label-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .ai-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>