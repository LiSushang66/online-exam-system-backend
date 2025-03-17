package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.model.vo.repo.RepoListVO;
import cn.org.alan.exam.model.vo.repo.RepoVO;
import cn.org.alan.exam.model.vo.exercise.ExerciseRepoVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author WeiJin
 * @since 2024-03-21
 */
@Repository
public interface RepoMapper extends BaseMapper<Repo> {

    /**
     * 分页查询题库
     *
     * @param page   分页项
     * @param title  题库名
     * @param userId 用户名
     * @return 响应结果
     */
    IPage<RepoVO> pagingRepo(@Param("page") IPage<RepoVO> page, @Param("title") String title,
                             @Param("userId") Integer userId);

    /**
     * 删除用户创建的题库
     *
     * @param userIds 用户id列表
     * @return 影响记录数
     */
    Integer deleteByUserIds(List<Integer> userIds);

    /**
     * 分页获取可刷题库列表
     *
     * @param page     分页信息
     * @param title    题库名
     * @param userList
     * @return 结果
     */
    IPage<ExerciseRepoVO> selectRepo(IPage<ExerciseRepoVO> page,
                                     String title, List<Integer> userList);

    List<RepoListVO> selectRepoList(String repoTitle, int userId);

    /**
     * 查找刷题记录
     * @param repoPage
     * @param userId
     * @param repoName
     * @return
     */
    Page<Repo> selectUserExerciseRecord(Page<Repo> repoPage, Integer userId, String repoName);
}
