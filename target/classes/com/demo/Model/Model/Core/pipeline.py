"""
Main Disaster Analysis Pipeline
"""

import os
import json
import glob
from typing import List, Tuple, Dict, Any

# Import from local modules
from Core.models import Post
from Data.data_loader import DataLoader
from Analyzer.sentiment_analyzer import SentimentAnalyzer
from Analyzer.damage_classifier import DamageClassifier
from Data.processors import SentimentAnalysisProcessor, DamageClassificationProcessor
from Analyzer.analyzers import TimeSeriesAnalyzer, DamageStatisticsAnalyzer
from Report.report_generator import ReportGenerator


class AdvancedDisasterAnalysisPipeline:
    """Pipeline nâng cao với ML/DL models"""

    def __init__(self,
                 sentiment_analyzer: SentimentAnalyzer,
                 damage_classifier: DamageClassifier,
                 batch_size: int = 32):
        self.sentiment_analyzer = sentiment_analyzer
        self.damage_classifier = damage_classifier
        self.batch_size = batch_size

    def run(self, input_file: str, output_dir: str = './results'):
        """Chạy toàn bộ pipeline"""
        os.makedirs(output_dir, exist_ok=True)

        print("\n" + "="*70)
        print("🚀 ADVANCED DISASTER ANALYSIS PIPELINE")
        print("="*70)

        # 1. Load dữ liệu
        print("\nBƯỚC 1: Load dữ liệu")
        loader = DataLoader(input_file)
        posts = loader.load()
        print(f"   Đã load {len(posts)} posts")

        # 2. Phân tích tâm lý
        print("\nBƯỚC 2: Phân tích tâm lý (Bài toán 1)")
        sentiment_processor = SentimentAnalysisProcessor(self.sentiment_analyzer)
        posts = sentiment_processor.process(posts, self.batch_size)

        # 3. Phân loại thiệt hại
        print("\nBƯỚC 3: Phân loại thiệt hại (Bài toán 2)")
        damage_processor = DamageClassificationProcessor(self.damage_classifier)
        posts = damage_processor.process(posts, self.batch_size)

        # 4. Phân tích kết quả
        print("\nBƯỚC 4: Tạo báo cáo phân tích")

        timeline_analyzer = TimeSeriesAnalyzer()
        sentiment_result = timeline_analyzer.analyze(posts)

        damage_analyzer = DamageStatisticsAnalyzer()
        damage_result = damage_analyzer.analyze(posts)

        # 5. Lưu kết quả
        report_generator = ReportGenerator('csv')

        report_generator.generate_sentiment_report(
            sentiment_result,
            f'{output_dir}/sentiment_timeline.csv'
        )

        report_generator.generate_damage_report(
            damage_result,
            f'{output_dir}/damage_statistics.csv'
        )

        # 6. Lưu processed posts
        self._save_processed_posts(posts, f'{output_dir}/processed_posts.json')

        print(f"\nHOÀN THÀNH! Kết quả lưu tại: {output_dir}")

        # 7. In summary
        self._print_summary(sentiment_result, damage_result)

        return posts, sentiment_result, damage_result

    def _save_processed_posts(self, posts: List[Post], output_path: str):
        """Lưu posts đã xử lý kèm kết quả phân tích"""
        data = []
        for post in posts:
            data.append({
                'id': post.post_id,
                'date': post.date.strftime('%Y-%m-%d') if post.date else None,
                'keyword': post.keyword,
                'content': post.content[:200] + '...',  # Truncate
                'sentiment': post.sentiment,
                'sentiment_scores': post.sentiment_scores.to_dict() if post.sentiment_scores else None,
                'damage_types': post.damage_types,
                'damage_scores': post.damage_scores
            })

        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)

        print(f"💾 Đã lưu processed posts: {output_path}")

    def _print_summary(self, sentiment_result, damage_result):
        """In tóm tắt kết quả"""
        print("\n" + "="*70)
        print("📋 TÓM TẮT KẾT QUẢ PHÂN TÍCH")
        print("="*70)

        # Sentiment summary
        print("\n🎭 PHÂN TÍCH TÂM LÝ (Bài toán 1):")
        date_range = sentiment_result['date_range']
        if date_range[0]:
            print(f"   🗓️  Khoảng thời gian: {date_range[0].strftime('%Y-%m-%d')} → {date_range[1].strftime('%Y-%m-%d')}")
        print(f"   📝 Tổng số posts: {sentiment_result['total_posts']}")

        summary = sentiment_result['summary']
        total = sum(summary.values())
        print(f"   😊 Tích cực: {summary['total_positive']} ({summary['total_positive']/total*100:.1f}%)")
        print(f"   😢 Tiêu cực: {summary['total_negative']} ({summary['total_negative']/total*100:.1f}%)")
        print(f"   😐 Trung lập: {summary['total_neutral']} ({summary['total_neutral']/total*100:.1f}%)")

        # Damage summary
        print("\n🏚️ PHÂN LOẠI THIỆT HẠI (Bài toán 2):")
        print(f"   📊 TOP 5 LOẠI THIỆT HẠI:")

        for i, (damage_type, count) in enumerate(damage_result['top_damages'][:5], 1):
            percentage = (count / damage_result['total_posts']) * 100
            confidence = damage_result['damage_confidence'].get(damage_type, 0)
            print(f"      {i}. {damage_type}: {count} posts ({percentage:.1f}%) - Confidence: {confidence:.3f}")


def find_latest_cleaned_file() -> str:
    """Find the most recent cleaned_*.csv file from DataPreprocessor output."""
    cleaned_files = glob.glob("cleaned_*.csv")
    if not cleaned_files:
        # Fallback to disaster_1000.csv if no cleaned files found
        default_path = os.path.abspath("disaster_1000.csv")
        if os.path.exists(default_path):
            print(f"No cleaned files found, using default: {default_path}")
            return default_path
        else:
            raise FileNotFoundError("No cleaned_*.csv files found and default disaster_1000.csv not found. Please run DataPreprocessor first.")

    # Return the most recently modified file
    latest_file = max(cleaned_files, key=os.path.getmtime)
    print(f"Using latest cleaned file: {latest_file}")
    return latest_file