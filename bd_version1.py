from DrissionPage import ChromiumPage
import time
import random
import csv

keyword = input('请输入关键字(多个关键字请用 ; 隔开)：')
num = int(input('请输入每个关键字获取数量:'))
page = ChromiumPage()
if ';' in keyword:
    key = keyword.split(';')
else:
    key = [keyword]
for keyword in key:
    page.get(
        'https://www.baidu.com/s?ie=utf-8&f=8&rsv_bp=1&rsv_idx=1&tn=baidu&wd=' + keyword + '&fenlei=256&rsv_pq=0xea9969830041365f&rsv_t=438eadvraAJaDKWtT20%2F6nYMNiqV12yf2rEcN35sbpJNJLTaiCAiABWu%2FB3X&rqlang=en&rsv_enter=1&rsv_dl=tb&rsv_sug3=10&rsv_sug1=20&rsv_sug7=101&rsv_sug2=0&rsv_btype=i&inputT=4858&rsv_sug4=9415',
        retry=99, show_errmsg=True, timeout=100)
    n = 0
    b = 0
    alist = []  # 存储文章链接
    while True:
        try:
            boxes = page.eles('.result c-container xpath-log new-pmd')  # 获取所有框
            for i in boxes:
                alist.append(i.ele('tag:a').link)  # 添加文章链接
                n += 1
                if n == num:
                    b = 1
                    break
            if b == 1:
                break
            page.ele('text:下一页 >').click()
            time.sleep(random.uniform(3, 6))
        except:
            if 1:
                break

    for i in alist:
        page.get(i, retry=5, show_errmsg=True, timeout=100)
        time.sleep(random.uniform(2, 5))
        txts = page.eles('tag:div')
        '''txt = ''
        for j in txts:
            txt += j.text
        txt = txt.replace('\n','')'''
        total_txt = ""
        for element in txts:
            txt = element.text
            if len(txt) > 20:
                total_txt += txt
        total_txt = total_txt.replace('\n','')

        title = page.ele('tag:title').text  # 文章标题

        data = [
            [title, total_txt]
        ]
        with open('data.csv', 'a', newline='', encoding='utf-8') as file:
            writer = csv.writer(file)
            writer.writerows(data)