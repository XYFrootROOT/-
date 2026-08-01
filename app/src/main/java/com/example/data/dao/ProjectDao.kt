package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClipEntity
import com.example.data.model.ExportedVideoEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects ORDER BY lastModified DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    // Track operations
    @Query("SELECT * FROM tracks WHERE projectId = :projectId ORDER BY `index` ASC")
    fun getTracksForProject(projectId: Long): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity): Long

    @Query("DELETE FROM tracks WHERE projectId = :projectId")
    suspend fun deleteTracksForProject(projectId: Long)

    // Clip operations
    @Query("SELECT * FROM clips WHERE trackId IN (SELECT id FROM tracks WHERE projectId = :projectId) ORDER BY startOffsetMs ASC")
    fun getClipsForProject(projectId: Long): Flow<List<ClipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClip(clip: ClipEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClips(clips: List<ClipEntity>)

    @Update
    suspend fun updateClip(clip: ClipEntity)

    @Query("DELETE FROM clips WHERE id = :clipId")
    suspend fun deleteClip(clipId: Long)

    @Query("DELETE FROM clips WHERE trackId IN (SELECT id FROM tracks WHERE projectId = :projectId)")
    suspend fun deleteClipsForProject(projectId: Long)

    // Exported videos
    @Query("SELECT * FROM exported_videos ORDER BY exportedAt DESC")
    fun getAllExportedVideos(): Flow<List<ExportedVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExportedVideo(video: ExportedVideoEntity): Long

    @Query("DELETE FROM exported_videos WHERE id = :id")
    suspend fun deleteExportedVideo(id: Long)
}
