"""
Main execution script for Disaster Analysis System
"""

import argparse
import os
from typing import List, Dict, Any
import pandas as pd
import sys
import io

# Import from local modules
from Core.pipeline import AdvancedDisasterAnalysisPipeline, find_latest_cleaned_file
from Analyzer.sentiment_analyzer import VietnameseSentimentPipeline, PhoBERTSentimentAnalyzer
from Analyzer.damage_classifier import HybridDamageClassifier, BERTMultiLabelDamageClassifier
from Core.trainer import ModelTrainer

try:
        # Cấu hình stdout (đầu ra console) để sử dụng UTF-8.
        # Phương pháp này hoạt động tốt nhất trên Python 3.7+
        sys.stdout.reconfigure(encoding='utf-8')
except (AttributeError, ValueError):
        # Dành cho các phiên bản cũ hơn hoặc môi trường không hỗ trợ reconfigure
        # Đôi khi cần phải đặt locale, nhưng reconfigure là cách hiện đại hơn.
        # Nếu vẫn lỗi, hãy thử chạy bằng PowerShell hoặc Terminal mới.
        pass

def example_usage_with_pretrained():
    """Ví dụ sử dụng với pretrained models"""

    # Option 1: Sử dụng pretrained sentiment pipeline (dễ nhất)
    print("🔧 Khởi tạo với pretrained models...")
    sentiment_analyzer = VietnameseSentimentPipeline()

    # Option 2: Sử dụng Hybrid (rule + ML nếu có)
    damage_classifier = HybridDamageClassifier(use_ml=False)  # Rule-based only

    # Chạy pipeline
    pipeline = AdvancedDisasterAnalysisPipeline(
        sentiment_analyzer=sentiment_analyzer,
        damage_classifier=damage_classifier,
        batch_size=16
    )

    posts, sentiment_result, damage_result = pipeline.run(
        input_file="data.csv",
        output_dir="./results"
    )

    return posts, sentiment_result, damage_result


def example_usage_with_finetuned():
    """Ví dụ sử dụng với fine-tuned models (hiệu suất tốt nhất)"""

    print("🔧 Khởi tạo với fine-tuned models...")

    # Sentiment analyzer với fine-tuned PhoBERT
    sentiment_analyzer = PhoBERTSentimentAnalyzer(
        model_name="vinai/phobert-base",
        fine_tuned_path="./models/phobert_sentiment_finetuned"  # Path đến model đã train
    )

    # Damage classifier với fine-tuned BERT
    damage_classifier = BERTMultiLabelDamageClassifier(
        model_path="./models/bert_damage_classifier",  # Path đến model đã train
        threshold=0.5
    )

    # Chạy pipeline
    pipeline = AdvancedDisasterAnalysisPipeline(
        sentiment_analyzer=sentiment_analyzer,
        damage_classifier=damage_classifier,
        batch_size=32  # Tăng batch size nếu có GPU
    )

    posts, sentiment_result, damage_result = pipeline.run(
        input_file="data.csv",
        output_dir="./results_ml"
    )

    return posts, sentiment_result, damage_result


def train_sentiment_model_example():
    """
    Ví dụ: Fine-tune PhoBERT cho sentiment analysis
    Cần chuẩn bị dữ liệu labeled trước
    """
    print("🎓 Hướng dẫn fine-tune sentiment model:")
    print("""
    1. Chuẩn bị dữ liệu labeled:
       - File CSV với cột: id, content, sentiment_label
       - sentiment_label: 'positive', 'negative', 'neutral'
       - Khuyến nghị: ít nhất 5000 samples

    2. Code mẫu fine-tune (sử dụng transformers Trainer):

    from transformers import AutoModelForSequenceClassification, TrainingArguments, Trainer
    from datasets import Dataset

    # Load data
    df = pd.read_csv('labeled_sentiment_data.csv')

    # Prepare dataset
    trainer = ModelTrainer()
    texts, labels = trainer.prepare_sentiment_data(posts, df['sentiment_label'].tolist())

    dataset = Dataset.from_dict({'text': texts, 'label': labels})

    # Load model
    model = AutoModelForSequenceClassification.from_pretrained(
        "vinai/phobert-base",
        num_labels=3  # positive, neutral, negative
    )

    # Training arguments
    training_args = TrainingArguments(
        output_dir="./models/phobert_sentiment_finetuned",
        num_train_epochs=3,
        per_device_train_batch_size=16,
        save_steps=500,
        logging_steps=100
    )

    # Train
    trainer = Trainer(
        model=model,
        args=training_args,
        train_dataset=dataset
    )

    trainer.train()
    model.save_pretrained("./models/phobert_sentiment_finetuned")

    3. Sau khi train xong, sử dụng:
       sentiment_analyzer = PhoBERTSentimentAnalyzer(
           fine_tuned_path="./models/phobert_sentiment_finetuned"
       )
    """)


def main():
    parser = argparse.ArgumentParser(description="Disaster Analysis System - ML/DL Enhanced")
    parser.add_argument("--csv", type=str, default=None, help="path to CSV with date,text,sentiment,damage_type (auto-detects latest cleaned_*.csv if not specified)")
    parser.add_argument("--epochs", type=int, default=3)
    parser.add_argument("--batch_size", type=int, default=16)
    parser.add_argument("--max_len", type=int, default=128)
    parser.add_argument("--lr", type=float, default=2e-5)
    parser.add_argument("--save_path", type=str, default="multitask_phobert.pt")
    parser.add_argument("--device", type=str, default=None, help="cuda or cpu")
    args = parser.parse_args()

    # Auto-detect latest cleaned file if not specified
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

    input_path = sys.argv[1]
    print(f"Processing file: {input_path}")

    if args.csv is None:
        args.csv = find_latest_cleaned_file()

    print(f"Using input file: {args.csv}")

    # Use pretrained models for simplicity
    sentiment_analyzer = VietnameseSentimentPipeline()
    damage_classifier = HybridDamageClassifier(use_ml=False)

    pipeline = AdvancedDisasterAnalysisPipeline(
        sentiment_analyzer=sentiment_analyzer,
        damage_classifier=damage_classifier,
        batch_size=args.batch_size
    )

    posts, sentiment_result, damage_result = pipeline.run(
        input_file=args.csv,
        output_dir="./results"
    )

    print("Analysis completed successfully!")


if __name__ == "__main__":

    print("""
    ╔════════════════════════════════════════════════════════════════╗
    ║   DISASTER ANALYSIS SYSTEM - ML/DL ENHANCED VERSION 2.0       ║
    ║   Hệ thống phân tích dữ liệu thảm họa sử dụng Deep Learning   ║
    ╚════════════════════════════════════════════════════════════════╝

    USAGE:

    # Cách 1: Sử dụng với pretrained models (Không cần train)
    from disaster_analysis import example_usage_with_pretrained
    posts, sentiment, damage = example_usage_with_pretrained()

    # Cách 2: Sử dụng với fine-tuned models (Hiệu suất cao nhất)
    from disaster_analysis import example_usage_with_finetuned
    posts, sentiment, damage = example_usage_with_finetuned()

    # Cách 3: Train models của riêng bạn
    from disaster_analysis import train_sentiment_model_example
    train_sentiment_model_example()

    DEPENDENCIES:
    pip install transformers torch pandas numpy scikit-learn

    TIPS:
    - Sử dụng GPU để tăng tốc (CUDA)
    - Fine-tune models trên dữ liệu của bạn để có kết quả tốt nhất
    - Batch size: 32-64 với GPU, 8-16 với CPU
    """)

    # Chạy demo
    try:
        main()
    except Exception as e:
        print(f"Lỗi: {e}")
        print("\n Cài đặt dependencies: pip install transformers torch pandas")