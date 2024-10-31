import csv

# 假设你的数据是以下格式的列表
data = [
    {"pageid": 154352, "title": "敏捷软件开发", "extract": "敏捷软件开发（英语：Agile software development）...", "related_topics": ["Scrum", "增量模型"]},
    {"pageid": 154353, "title": "Scrum", "extract": "Scrum 是敏捷开发中的一种框架..."},
    {"pageid": 154354, "title": "增量模型", "extract": "增量模型是一种逐步构建的开发模型..."}
]

# 写入节点文件
with open("nodes.csv", "w", newline="", encoding="utf-8") as nodes_file:
    writer = csv.writer(nodes_file)
    writer.writerow(["id", "title", "content"])
    for entry in data:
        writer.writerow([entry["pageid"], entry["title"], entry["extract"]])

# 写入关系文件
with open("relationships.csv", "w", newline="", encoding="utf-8") as rels_file:
    writer = csv.writer(rels_file)
    writer.writerow(["source_id", "target_id", "relationship_type"])
    for entry in data:
        for related in entry.get("related_topics", []):
            related_entry = next((item for item in data if item["title"] == related), None)
            if related_entry:
                writer.writerow([entry["pageid"], related_entry["pageid"], "RELATED_TO"])
