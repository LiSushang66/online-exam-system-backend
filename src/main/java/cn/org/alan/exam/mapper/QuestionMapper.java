package cn.org.alan.exam.mapper;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.vo.QuestionVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 插入试题并获取自增主键
     *
     * @param question 试题试题对象
     * @return 主键
     */
    Integer insertGetId(Question question);

    /**
     * 分页获取试题信息
     *
     * @param page   分页信息
     * @param content  试题模糊查询
     * @param repoId 题库id
     * @param type 试题类型
     * @param userId 用户id
     * @return 放回结果
     */
    IPage<QuestionVO> pagingQuestion(IPage<QuestionVO> page, String content, Integer repoId, Integer type, Integer userId);

    /**
     * 根据试题id获取单题详情
     * @param id 试题id
     * @return 结果集
     */
    QuestionVO selectSingle(Integer id);
}
