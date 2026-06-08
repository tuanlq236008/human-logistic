"""
Data Loader for CSV files
"""

import pandas as pd
from typing import List
from Core.models import Post


class DataLoader:
    """Đọc và tiền xử lý dữ liệu từ CSV"""

    def __init__(self, file_path: str):
        self.file_path = file_path

    def load(self) -> List[Post]:
        """Đọc CSV và chuyển thành danh sách Post objects"""
        df = pd.read_csv(self.file_path, encoding='utf-8')
        posts = []

        for _, row in df.iterrows():
            try:
                post = Post(
                    post_id=str(row['id']),
                    authors=str(row['authors']),
                    keyword=str(row['keyword']),
                    date=str(row['Date']),
                    reactions=int(row.get('Reactions', 0)),
                    comments=int(row.get('COMMENT', 0)),
                    shares=int(row.get('Share', 0)),
                    content=str(row['content']),
                    
                )
                posts.append(post)
            except Exception as e:
                print(f"⚠️ Lỗi khi đọc row {row.get('id', 'unknown')}: {e}")
                continue

        return posts