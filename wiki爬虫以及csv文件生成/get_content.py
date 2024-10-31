import requests

url = "https://zh.wikipedia.org/w/api.php"
params = {
    "action": "query",
    "prop": "extracts",
    "explaintext": True,
    "titles": "敏捷软件开发",
    "format": "json",
    "variant": "zh-cn"
}

response = requests.get(url, params=params)
if response.status_code == 200:
    data = response.json()
    page_content = list(data['query']['pages'].values())[0]['extract']
    print(page_content)
else:
    print("请求失败，状态码：", response.status_code)
