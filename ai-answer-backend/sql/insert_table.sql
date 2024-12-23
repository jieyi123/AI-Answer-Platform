
-- 插入post表相关数据
insert into post ( title, content, tags, userId)
values ('夜读好书，邂逅别样世界',
        '夜晚，当周遭安静下来，翻开一本好书，便如同开启了一场奇妙的冒险。跟随书中的文字，时而置身于古老神秘的城堡，探索那些尘封的秘密；时而踏入奇幻绚丽的魔法世界，与神奇的生物为伴。夜读的时光，让心灵得到滋养，邂逅了一个又一个别样精彩的世界。',
        '["夜读","书籍","奇幻世界"]',1863769896358084610);
insert into post (title, content, tags, userId)
values (' 雨中读书，品味别样韵味 ',
        ' 窗外淅淅沥沥下着雨，雨滴敲打着玻璃，营造出一种独特的静谧氛围。此时手捧一本书，那感觉格外美妙。仿佛书中的故事也被这雨赋予了别样的韵味，跟随文字的描述，可能会置身于江南水乡的烟雨朦胧中，感受那份温婉与柔情；也可能会身处异国他乡的古老街巷，在雨中聆听岁月的回响。雨中读书的时光，别有一番滋味在心头。',
        '["雨中阅读","书籍","别样氛围"]',1863769896358084610);
insert into post (title, content, tags, userId)
values (' 晨读一刻，开启智慧之门 ',
        ' 清晨，当第一缕阳光洒下，伴着清新的空气翻开一本好书，就像是打开了一扇通往智慧世界的大门。书中的每一个段落、每一句话都像是一把钥匙，引领着我们去探索未知的知识领域。时而沉浸在深邃的哲学思考中，时而领略科学世界的奇妙奥秘。晨读的这短暂一刻，让我们的心灵得到启迪，为新的一天注入满满的正能量。',
        '["晨读","书籍","知识探索"]',1863769896358084610);
insert into post (title, content, tags, userId)
values (' 静享午后，沉浸书香之境 ',
        ' 午后的阳光透过窗户洒在书页上，营造出一片宁静而惬意的氛围。轻轻翻开一本书，仿佛踏入了一片别样的天地。有时会跟随主人公穿梭在历史的长河中，见证那些波澜壮阔的大事件；有时又会漫步于诗意的田园风光里，感受着大自然的美好与宁静。在这午后读书的时光里，让思绪尽情遨游，沉浸在这充满魅力的书香之境中，收获内心的平和与满足。',
        '["午后阅读","书籍","宁静时光"]',1863769896358084610);

-- 插入user相关数据
INSERT INTO user (userAccount, userPassword, userName, userAvatar, userProfile)
VALUES
    ('zhangsan', '56191e5f69f082ef3e10280f2ad31672', 'zhangsan', 'https://cravatar.cn/avatar/zhangsan@example.com', '我是一名java开发程序员，能够熟练应用spring框架来进行开发'),
    ('lisi', '56191e5f69f082ef3e10280f2ad31672', 'lisi', 'https://cravatar.cn/avatar/lisi@example.com', '我是一名前端开发工程师，熟悉Vue和React框架'),
    ('wangwu', '56191e5f69f082ef3e10280f2ad31672', 'wangwu', 'https://cravatar.cn/avatar/wangwu@example.com', '我是一名后端开发工程师，擅长Python和Django框架'),
    ('zhaoliu', '56191e5f69f082ef3e10280f2ad31672', 'zhaoliu', 'https://cravatar.cn/avatar/zhaoliu@example.com', '我是一名全栈开发工程师，拥有丰富的项目开发经验'),
    ('tianqi', '56191e5f69f082ef3e10280f2ad31672', 'tianqi', 'https://cravatar.cn/avatar/tianqi@example.com', '我是一名数据分析师，熟悉SQL和大数据处理技术');