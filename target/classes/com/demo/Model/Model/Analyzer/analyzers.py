"""
Analysis Classes for Time Series and Statistics
"""

from collections import defaultdict
from typing import List, Dict, Any
import numpy as np

# Import from local modules
from Core.models import Post


class TimeSeriesAnalyzer:
    """Phân tích tâm lý theo thời gian (Bài toán 1)"""

    def analyze(self, posts: List[Post]) -> Dict[str, Any]:
        daily_sentiments = defaultdict(lambda: {'positive': 0, 'negative': 0, 'neutral': 0, 'total': 0})

        for post in posts:
            if post.date and post.sentiment:
                date_key = post.date.strftime('%Y-%m-%d')
                daily_sentiments[date_key][post.sentiment] += 1
                daily_sentiments[date_key]['total'] += 1

        timeline = []
        for date in sorted(daily_sentiments.keys()):
            stats = daily_sentiments[date]
            total = stats['total']

            timeline.append({
                'date': date,
                'positive': stats['positive'],
                'negative': stats['negative'],
                'neutral': stats['neutral'],
                'positive_pct': (stats['positive'] / total * 100) if total > 0 else 0,
                'negative_pct': (stats['negative'] / total * 100) if total > 0 else 0,
                'neutral_pct': (stats['neutral'] / total * 100) if total > 0 else 0,
                'total': total
            })

        valid_dates = [p.date for p in posts if p.date]

        return {
            'timeline': timeline,
            'total_posts': len(posts),
            'date_range': (min(valid_dates), max(valid_dates)) if valid_dates else (None, None),
            'summary': {
                'total_positive': sum(d['positive'] for d in timeline),
                'total_negative': sum(d['negative'] for d in timeline),
                'total_neutral': sum(d['neutral'] for d in timeline),
            }
        }


class DamageStatisticsAnalyzer:
    """Phân tích thống kê thiệt hại (Bài toán 2)"""

    def analyze(self, posts: List[Post]) -> Dict[str, Any]:
        damage_counts = defaultdict(int)
        damage_confidence = defaultdict(list)

        for post in posts:
            for damage_type in post.damage_types:
                damage_counts[damage_type] += 1
                if post.damage_scores:
                    damage_confidence[damage_type].append(post.damage_scores.get(damage_type, 0))

        # Tính avg confidence
        avg_confidence = {}
        for damage_type, scores in damage_confidence.items():
            avg_confidence[damage_type] = np.mean(scores) if scores else 0

        sorted_damages = sorted(damage_counts.items(), key=lambda x: x[1], reverse=True)

        return {
            'damage_counts': dict(damage_counts),
            'damage_confidence': avg_confidence,
            'top_damages': sorted_damages,
            'total_posts': len(posts),
            'summary': {
                'most_common': sorted_damages[0] if sorted_damages else None,
                'total_damage_mentions': sum(damage_counts.values())
            }
        }