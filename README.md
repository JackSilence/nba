目的: 每日定時排程發送Ptt Web BBS之NBA板的比賽結果

機制: 將此應用程式部署在Heroku上, 透過UptimeRobot定時監控避免使用Heroku免費帳號的休眠問題

nba - 透過Selenium + Headless Chrome自動抓取當日比賽結果並處理字體問題, 用SendGrid寄出通知信.

ExecuteController - 手動執行Task使用. 將Spring ApplicationContext中符合名稱的Bean取出呼叫execute方法執行
