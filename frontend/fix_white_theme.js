const fs = require('fs');

function replaceStyle(file, newStyle) {
    if (!fs.existsSync(file)) return;
    let code = fs.readFileSync(file, 'utf8');
    // Replace whole <style> block
    if (/<style.*?>[\s\S]*?<\/style>/.test(code)) {
        code = code.replace(/<style.*?>[\s\S]*?<\/style>/m, newStyle);
        fs.writeFileSync(file, code, 'utf8');
        console.log('Fixed styles in ' + file);
    }
}

const mainLayoutStyles = \
<style lang="scss" scoped>
.app-shell {
  min-height: 100vh;
  background: #f4f6f8;
  color: #1e293b;
  font-family: system-ui, -apple-system, sans-serif;
}

.topbar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
}

.brand {
  font-weight: 600;
  font-size: 18px;
  display: flex;
  align-items: center;
  color: #0f172a;
}

.sidebar {
  width: 240px;
  background: #ffffff;
  border-right: 1px solid #e2e8f0;
}

:deep(.el-menu) {
  border-right: none;
  background: transparent !important;
}

:deep(.el-menu-item) {
  color: #475569;
  border-radius: 8px;
  margin: 4px 12px;
  transition: all 0.2s ease;
  height: 48px;
  line-height: 48px;

  &:hover {
    background: #f8fafc !important;
    color: #3b82f6;
  }

  &.is-active {
    background: #eff6ff !important;
    color: #2563eb;
    font-weight: 600;
  }
}

:deep(.el-button--default) {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  color: #475569;
  border-radius: 6px;

  &:hover {
    background: #f1f5f9;
    color: #3b82f6;
    border-color: #bfdbfe;
  }
}

:deep(.el-button--primary) {
  background: #3b82f6;
  border-color: #3b82f6;
  color: #ffffff;
  border-radius: 6px;
  font-weight: 500;

  &:hover {
    background: #2563eb;
    border-color: #2563eb;
  }
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.content {
  flex: 1;
  padding: 24px;
  min-width: 0;
  background: #f4f6f8;
}
</style>
\;

const homeStyles = \
<style scoped lang="scss">
.home {
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  margin-bottom: 32px;
}

.title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 20px;
  display: flex;
  align-items: center;
  color: #0f172a;

  .title-icon {
    margin-right: 12px;
    color: #3b82f6;
  }
}

.subtitle-alert {
  border-radius: 8px;
  background-color: #ffffff !important;
  border: 1px solid #e2e8f0;
  
  :deep(.el-alert__title) {
    font-size: 14px;
    line-height: 1.6;
    color: #475569;
  }
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.card {
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 12px;
  border: 1px solid #e2e8f0 !important;
  background: #ffffff !important;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.05) !important;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -2px rgba(0, 0, 0, 0.025) !important;
    border-color: #bfdbfe !important;
  }

  :deep(.el-card__header) {
    padding: 20px 24px 16px;
    background: transparent;
    border-bottom: 1px dashed #e2e8f0;
  }

  :deep(.el-card__body) {
    padding: 20px 24px;
    background: transparent;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #0f172a;
}

.card-icon {
  color: #3b82f6;
  flex-shrink: 0;
}

.card-title {
  font-weight: 600;
  font-size: 18px;
}

.card-desc {
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}

.hint {
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
  margin-top: 40px;

  strong {
    color: #f59e0b;
    font-weight: 500;
  }
}
</style>
\;

replaceStyle('src/layouts/MainLayout.vue', mainLayoutStyles);
replaceStyle('src/views/HomeView.vue', homeStyles);
replaceStyle('src/views/HomeViewModern.vue', homeStyles.replace('.home {', '.home {\\n  padding: 0;'));

