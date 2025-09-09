# Имя для конечного JAR файла (без расширения)
TARGET = main

# Компилятор Kotlin
KOTLINC = kotlinc

# Директории
SRC_DIR = src
BUILD_DIR = build
DOCS_DIR = docs

# Исходники Kotlin
SOURCES = $(wildcard $(SRC_DIR)/*.kt)
JAR_FILE = $(BUILD_DIR)/$(TARGET).jar

# Документация
DOC_SOURCE = $(DOCS_DIR)/index.typ
DOC_OUTPUT = $(BUILD_DIR)/index.pdf

# Цели, которые не являются файлами
.PHONY: all run docs clean

# Цель по умолчанию: собрать приложение и документацию
all: $(JAR_FILE) $(DOC_OUTPUT)

# Цель для запуска приложения
run: $(JAR_FILE)
	@echo "🚀 Запуск приложения..."
	@java -jar $(JAR_FILE)

# Цель для сборки документации
docs: $(DOC_OUTPUT)

# Правило для сборки JAR файла из исходников Kotlin
$(JAR_FILE): $(SOURCES)
	@mkdir -p $(BUILD_DIR)
	@echo "🤖 Компиляция Kotlin в $(JAR_FILE)..."
	@$(KOTLINC) $(SOURCES) -include-runtime -d $(JAR_FILE)

# Правило для сборки документации
$(DOC_OUTPUT): $(DOC_SOURCE)
	@mkdir -p $(BUILD_DIR)
	@echo "🗒️  Сборка документации..."
	@typst compile --root .. $< $@

# Цель для очистки сборочной директории
clean:
	@echo "🔥 Очистка проекта..."
	@rm -rf $(BUILD_DIR)
