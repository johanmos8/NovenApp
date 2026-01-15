package com.mirkwood.novenapp.presentation.screens.lyrics

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.model.Song
import com.mirkwood.novenapp.ui.theme.NovenAppTheme


@Composable
fun SongListScreen(onAction: (Int) -> Unit, songs: List<Song>, onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(songs) { song ->
            SongItem(song) {
                Log.d("SongList", "$song")
                onAction(song.id)
            }
        }
    }
}

@Composable
fun SongItem(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.music_note_24px),
            contentDescription = stringResource(R.string.text_music),
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(song.title, fontWeight = FontWeight.Bold)
            song.artist?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@PreviewAllPhones
@Composable
fun PreviewSongList() {
    val mockList = listOf(
        Song(
            id = 1,
            title = "Noche de paz",
            lyrics = "String"
        ),
        Song(
            id = 2,
            title = "Noche de paz",
            lyrics = "String"
        )
    )
    NovenAppTheme {
        SongListScreen(
            songs = mockList,
            onAction = {},
            onBack = {}
        )
    }
}
