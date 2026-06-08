"""
Report Generator for Analysis Results
"""

import json
import pandas as pd
from typing import Dict, Any


class ReportGenerator:
    """Tạo báo cáo chi tiết với visualizations"""

    def __init__(self, output_format: str = 'json'):
        self.output_format = output_format

    def generate_sentiment_report(self, analysis_result: Dict[str, Any], output_path: str):
        """Tạo báo cáo phân tích tâm lý"""
        if self.output_format == 'json':
            with open(output_path, 'w', encoding='utf-8') as f:
                json.dump(analysis_result, f, ensure_ascii=False, indent=2, default=str)

        elif self.output_format == 'csv':
            df = pd.DataFrame(analysis_result['timeline'])
            df.to_csv(output_path, index=False, encoding='utf-8-sig')

        print(f"📊 Đã lưu báo cáo sentiment: {output_path}")

    def generate_damage_report(self, analysis_result: Dict[str, Any], output_path: str):
        """Tạo báo cáo phân loại thiệt hại"""
        if self.output_format == 'json':
            with open(output_path, 'w', encoding='utf-8') as f:
                json.dump(analysis_result, f, ensure_ascii=False, indent=2)

        elif self.output_format == 'csv':
            rows = []
            for damage_type, count in analysis_result['top_damages']:
                confidence = analysis_result['damage_confidence'].get(damage_type, 0)
                rows.append({
                    'total_posts': analysis_result['total_posts'],
                    'damage_type': damage_type,
                    'post_count': count,
                    'average_confidence': f"{confidence:.3f}"
                })

            df = pd.DataFrame(rows)
            df.to_csv(output_path, index=False, encoding='utf-8-sig')

        print(f"📊 Đã lưu báo cáo damage: {output_path}")