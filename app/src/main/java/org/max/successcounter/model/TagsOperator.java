package org.max.successcounter.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.excercise.Result;
import org.max.successcounter.model.excercise.ResultTags;
import org.max.successcounter.model.excercise.Tag;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public enum TagsOperator
{
    instance;

    private Dao<Tag, Integer> tagDao;
    private Dao<ResultTags, Integer> crossDao;

    public void init(DatabaseHelper db) throws SQLException
    {
        tagDao = db.getDao(Tag.class);
        crossDao = db.getDao(ResultTags.class);
    }

    public List<Tag> getAll() throws SQLException
    {
        return tagDao.queryForAll();
    }

    public int addTag(Tag tag) throws SQLException
    {
        return tagDao.create(tag);
    }

    public void deleteTag(Tag tag) throws SQLException
    {
        tagDao.delete(tag);
    }

    public List<Tag> getTagsForResult(Result res) throws SQLException
    {
        QueryBuilder<Tag, Integer> tqb = tagDao.queryBuilder();
        QueryBuilder<ResultTags, Integer> rqb = crossDao.queryBuilder();
        List<ResultTags> rt = rqb.where().eq("resultID", res.getId()).query();
        return tqb.join("id", "tagID", rqb).query();
    }

    /**
     * Set tags for the result.
     * The old set of tags will be deleted before inserting of the new tag set
     *
     * @param result
     * @return
     * @throws SQLException
     */
    public int setTagsForResult(Result result) throws SQLException
    {
        DeleteBuilder<ResultTags, Integer> dlb = crossDao.deleteBuilder();
        dlb.where().eq("resultID", result.getId());
        dlb.delete();
        List<ResultTags> items = new ArrayList<>();
        result.getTags().stream().forEach(item -> items.add(new ResultTags(result.getId(), item.getId())));
        return crossDao.create(items);
    }

    public void removeTagsFromResult(Result result, List<Tag> toDelete) throws SQLException
    {
        DeleteBuilder<ResultTags, Integer> db = crossDao.deleteBuilder();
        Where<ResultTags, Integer> where = db.where().eq("resultID", result.getId());

        for (Tag tag : toDelete)
            where.and().eq("tagID", tag.getId());

        db.delete();
        deleteUnusedTags();
    }

    /**
     * Delete unused tags
     * @return count of deleted
     * @throws SQLException
     */
    public int deleteUnusedTags() throws SQLException
    {
        QueryBuilder<Tag, Integer> qTags = tagDao.queryBuilder();

        QueryBuilder<ResultTags, Integer> qResultTags = crossDao.queryBuilder();
        qResultTags.selectColumns("tagID").distinct();

        List<ResultTags> asd = qResultTags.query();

        List<Tag> tags = qTags.where().notIn("id", qResultTags ).query();

        return tagDao.delete(tags);
    }
}